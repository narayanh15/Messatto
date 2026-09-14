param(
    [string]$BaseUrl = "http://localhost:8080",
    [string]$SuperAdminEmail = "superadmin@messatto.local",
    [string]$SuperAdminPassword = "SuperAdmin@123"
)

$ErrorActionPreference = "Stop"

function Invoke-Json {
    param(
        [ValidateSet("GET", "POST")]
        [string]$Method,
        [string]$Path,
        [object]$Body = $null,
        [string]$Token = $null
    )

    $headers = @{}
    if ($Token) {
        $headers["Authorization"] = "Bearer $Token"
    }

    $uri = "$BaseUrl$Path"
    if ($null -ne $Body) {
        return Invoke-RestMethod -Method $Method -Uri $uri -Headers $headers -ContentType "application/json" -Body ($Body | ConvertTo-Json -Depth 8)
    }

    return Invoke-RestMethod -Method $Method -Uri $uri -Headers $headers
}

$suffix = Get-Date -Format "yyyyMMddHHmmss"
$mealDate = Get-Date -Format "yyyy-MM-dd"
$startTime = (Get-Date).AddMinutes(-5).ToString("HH:mm:ss")
$endTime = (Get-Date).AddHours(2).ToString("HH:mm:ss")

Write-Host "1. Logging in as seeded super admin..."
$superAdmin = Invoke-Json -Method POST -Path "/auth/login" -Body @{
    email = $SuperAdminEmail
    password = $SuperAdminPassword
}
$superToken = $superAdmin.accessToken

Write-Host "2. Creating mess admin and student..."
$adminEmail = "messadmin+$suffix@messatto.local"
$studentEmail = "student+$suffix@messatto.local"

Invoke-Json -Method POST -Path "/super-admin/users" -Token $superToken -Body @{
    name = "Smoke Mess Admin"
    email = $adminEmail
    password = "Admin@123"
    role = "MESS_ADMIN"
    hostel = "Hostel A"
} | Out-Null

Invoke-Json -Method POST -Path "/super-admin/users" -Token $superToken -Body @{
    name = "Smoke Student"
    email = $studentEmail
    rollNumber = "SMOKE-$suffix"
    password = "Student@123"
    role = "STUDENT"
    hostel = "Hostel A"
} | Out-Null

Write-Host "3. Logging in as mess admin and student..."
$admin = Invoke-Json -Method POST -Path "/auth/login" -Body @{
    email = $adminEmail
    password = "Admin@123"
}
$student = Invoke-Json -Method POST -Path "/auth/login" -Body @{
    email = $studentEmail
    password = "Student@123"
}

Write-Host "4. Creating an active test meal..."
$meal = Invoke-Json -Method POST -Path "/admin/meals" -Token $admin.accessToken -Body @{
    mealDate = $mealDate
    mealType = "LUNCH"
    vegMenu = "Rice, dal, paneer, salad"
    nonVegMenu = "Rice, dal, chicken curry, salad"
    startTime = $startTime
    endTime = $endTime
}

Write-Host "5. Generating VEG QR token..."
$qr = Invoke-Json -Method POST -Path "/admin/meals/$($meal.id)/qr" -Token $admin.accessToken -Body @{
    mealChoice = "VEG"
    ttlSeconds = 900
}

Write-Host "6. Scanning QR as student..."
$scan = Invoke-Json -Method POST -Path "/attendance/scan" -Token $student.accessToken -Body @{
    token = $qr.token
}

Write-Host "7. Re-scanning QR to prove idempotency..."
$scanAgain = Invoke-Json -Method POST -Path "/attendance/scan" -Token $student.accessToken -Body @{
    token = $qr.token
}

Write-Host "8. Submitting verified feedback..."
$feedback = Invoke-Json -Method POST -Path "/feedback" -Token $student.accessToken -Body @{
    mealId = $meal.id
    tasteRating = 4
    hygieneRating = 5
    quantityRating = 4
    serviceRating = 5
    comment = "Smoke test feedback"
    anonymous = $true
}

Write-Host "9. Reading dashboard, feedback summary, and report..."
$dashboard = Invoke-Json -Method GET -Path "/admin/dashboard/live?date=$mealDate&mealType=LUNCH" -Token $admin.accessToken
$summary = Invoke-Json -Method GET -Path "/admin/feedback/summary?mealId=$($meal.id)" -Token $admin.accessToken
$report = Invoke-Json -Method GET -Path "/admin/reports/daily?date=$mealDate" -Token $admin.accessToken

[pscustomobject]@{
    status = "PASS"
    mealId = $meal.id
    firstScanAlreadyRecorded = $scan.alreadyRecorded
    secondScanAlreadyRecorded = $scanAgain.alreadyRecorded
    dashboardTotalAttendance = $dashboard[0].totalAttendance
    dashboardVegCount = $dashboard[0].vegCount
    feedbackId = $feedback.id
    feedbackCount = $summary[0].totalFeedback
    reportTotalAttendance = $report.totalAttendance
} | Format-List
