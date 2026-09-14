CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(120) NOT NULL,
    email VARCHAR(180) NOT NULL,
    roll_number VARCHAR(60),
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(32) NOT NULL CHECK (role IN ('STUDENT', 'MESS_ADMIN', 'SUPER_ADMIN')),
    hostel VARCHAR(120),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX uk_users_email_lower ON users (lower(email));
CREATE UNIQUE INDEX uk_users_roll_number ON users (roll_number) WHERE roll_number IS NOT NULL;

CREATE TABLE meals (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    meal_date DATE NOT NULL,
    meal_type VARCHAR(32) NOT NULL CHECK (meal_type IN ('BREAKFAST', 'LUNCH', 'DINNER')),
    veg_menu TEXT NOT NULL,
    non_veg_menu TEXT NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    created_by UUID NOT NULL REFERENCES users(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT chk_meals_time_window CHECK (end_time > start_time),
    CONSTRAINT uk_meals_date_type UNIQUE (meal_date, meal_type)
);

CREATE TABLE qr_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    meal_id UUID NOT NULL REFERENCES meals(id) ON DELETE CASCADE,
    meal_choice VARCHAR(32) NOT NULL CHECK (meal_choice IN ('VEG', 'NON_VEG')),
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    expires_at TIMESTAMPTZ NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_qr_tokens_meal_choice ON qr_tokens (meal_id, meal_choice);

CREATE TABLE meal_attendance (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id UUID NOT NULL REFERENCES users(id),
    meal_id UUID NOT NULL REFERENCES meals(id) ON DELETE CASCADE,
    meal_choice VARCHAR(32) NOT NULL CHECK (meal_choice IN ('VEG', 'NON_VEG')),
    scanned_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uk_attendance_student_meal UNIQUE (student_id, meal_id)
);

CREATE INDEX idx_attendance_meal_choice ON meal_attendance (meal_id, meal_choice);
CREATE INDEX idx_attendance_student_scanned_at ON meal_attendance (student_id, scanned_at DESC);

CREATE TABLE feedback (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id UUID NOT NULL REFERENCES users(id),
    meal_id UUID NOT NULL REFERENCES meals(id) ON DELETE CASCADE,
    taste_rating SMALLINT NOT NULL CHECK (taste_rating BETWEEN 1 AND 5),
    hygiene_rating SMALLINT NOT NULL CHECK (hygiene_rating BETWEEN 1 AND 5),
    quantity_rating SMALLINT NOT NULL CHECK (quantity_rating BETWEEN 1 AND 5),
    service_rating SMALLINT NOT NULL CHECK (service_rating BETWEEN 1 AND 5),
    comment VARCHAR(1000),
    anonymous BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uk_feedback_student_meal UNIQUE (student_id, meal_id)
);

CREATE INDEX idx_feedback_meal ON feedback (meal_id);
