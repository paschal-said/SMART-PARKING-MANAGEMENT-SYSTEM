CREATE EXTENSION IF NOT EXISTS btree_gist;

CREATE TABLE users (
    user_id        BIGSERIAL PRIMARY KEY,
    full_name      VARCHAR(120) NOT NULL,
    email          VARCHAR(160) NOT NULL UNIQUE,
    phone          VARCHAR(30),
    password_hash  VARCHAR(255) NOT NULL,
    role           VARCHAR(20)  NOT NULL CHECK (role IN ('DRIVER','ADMIN')),
    is_active      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE vehicles (
    vehicle_id     BIGSERIAL PRIMARY KEY,
    user_id        BIGINT       NOT NULL REFERENCES users(user_id),
    plate_number   VARCHAR(20)  NOT NULL,
    vehicle_type   VARCHAR(40)  NOT NULL,
    vehicle_model  VARCHAR(80),
    is_active      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);
CREATE UNIQUE INDEX uq_vehicles_active_plate ON vehicles(plate_number) WHERE is_active;
CREATE INDEX idx_vehicles_user ON vehicles(user_id);

CREATE TABLE floors (
    floor_id    BIGSERIAL PRIMARY KEY,
    floor_name  VARCHAR(60) NOT NULL UNIQUE
);

CREATE TABLE parking_slots (
    slot_id               BIGSERIAL PRIMARY KEY,
    floor_id              BIGINT      NOT NULL REFERENCES floors(floor_id),
    slot_number           VARCHAR(20) NOT NULL,
    occupancy_status      VARCHAR(20) NOT NULL DEFAULT 'UNKNOWN'
                          CHECK (occupancy_status IN ('AVAILABLE','OCCUPIED','UNKNOWN')),
    occupancy_updated_at  TIMESTAMPTZ,
    is_active             BOOLEAN     NOT NULL DEFAULT TRUE,
    UNIQUE (floor_id, slot_number)
);

CREATE TABLE rates (
    rate_id         BIGSERIAL PRIMARY KEY,
    name            VARCHAR(80)    NOT NULL,
    price_per_hour  NUMERIC(12,2)  NOT NULL CHECK (price_per_hour >= 0),
    currency        VARCHAR(3)     NOT NULL DEFAULT 'TZS',
    is_active       BOOLEAN        NOT NULL DEFAULT TRUE
);

CREATE TABLE reservations (
    reservation_id  BIGSERIAL PRIMARY KEY,
    user_id         BIGINT        NOT NULL REFERENCES users(user_id),
    vehicle_id      BIGINT        NOT NULL REFERENCES vehicles(vehicle_id),
    slot_id         BIGINT        NOT NULL REFERENCES parking_slots(slot_id),
    start_time      TIMESTAMPTZ   NOT NULL,
    end_time        TIMESTAMPTZ   NOT NULL,
    amount          NUMERIC(12,2) NOT NULL CHECK (amount >= 0),
    status          VARCHAR(24)   NOT NULL
                    CHECK (status IN ('PENDING_PAYMENT','CONFIRMED','CANCELLED','EXPIRED','COMPLETED')),
    expires_at      TIMESTAMPTZ,
    created_at      TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    CHECK (end_time > start_time),
    CONSTRAINT no_slot_overlap EXCLUDE USING gist (
        slot_id WITH =,
        tstzrange(start_time, end_time) WITH &&
    ) WHERE (status IN ('PENDING_PAYMENT','CONFIRMED'))
);
CREATE INDEX idx_reservations_status ON reservations(status);
CREATE INDEX idx_reservations_start ON reservations(start_time);

CREATE TABLE payments (
    payment_id             BIGSERIAL PRIMARY KEY,
    reservation_id         BIGINT        NOT NULL REFERENCES reservations(reservation_id),
    amount                 NUMERIC(12,2) NOT NULL CHECK (amount >= 0),
    payment_method         VARCHAR(30)   NOT NULL
                           CHECK (payment_method IN ('MPESA','MIXX','AIRTEL_MONEY','CARD_DEMO')),
    transaction_reference  VARCHAR(100)  UNIQUE,
    payment_status         VARCHAR(20)   NOT NULL
                           CHECK (payment_status IN ('PENDING','PAID','FAILED','REFUNDED')),
    payment_date           TIMESTAMPTZ
);

CREATE TABLE esp32_devices (
    device_id      BIGSERIAL PRIMARY KEY,
    device_name    VARCHAR(80)  NOT NULL,
    mac_address    VARCHAR(40)  UNIQUE,
    api_key_hash   VARCHAR(255),
    floor_id       BIGINT REFERENCES floors(floor_id),
    device_status  VARCHAR(20)  NOT NULL DEFAULT 'OFFLINE'
                   CHECK (device_status IN ('ONLINE','OFFLINE','FAULT')),
    last_seen      TIMESTAMPTZ
);

CREATE TABLE gates (
    gate_id          BIGSERIAL PRIMARY KEY,
    gate_name        VARCHAR(80) NOT NULL,
    gate_type        VARCHAR(10) NOT NULL CHECK (gate_type IN ('ENTRY','EXIT')),
    esp32_device_id  BIGINT REFERENCES esp32_devices(device_id),
    gate_status      VARCHAR(20) NOT NULL DEFAULT 'UNKNOWN'
                     CHECK (gate_status IN ('OPEN','CLOSED','UNKNOWN','FAULT'))
);

CREATE TABLE anpr_logs (
    anpr_id             BIGSERIAL PRIMARY KEY,
    gate_id             BIGINT REFERENCES gates(gate_id),
    detected_plate      VARCHAR(20),
    vehicle_detected    BOOLEAN     NOT NULL,
    confidence_score    NUMERIC(5,4),
    image_path          TEXT,
    recognition_status  VARCHAR(20) NOT NULL
                        CHECK (recognition_status IN ('MATCHED','UNMATCHED','LOW_CONFIDENCE','ERROR')),
    reason              TEXT,
    detected_at         TIMESTAMPTZ NOT NULL
);
CREATE INDEX idx_anpr_plate ON anpr_logs(detected_plate);

CREATE TABLE parking_sessions (
    session_id      BIGSERIAL PRIMARY KEY,
    reservation_id  BIGINT      NOT NULL REFERENCES reservations(reservation_id),
    vehicle_id      BIGINT      NOT NULL REFERENCES vehicles(vehicle_id),
    slot_id         BIGINT      NOT NULL REFERENCES parking_slots(slot_id),
    entry_gate_id   BIGINT      NOT NULL REFERENCES gates(gate_id),
    exit_gate_id    BIGINT      REFERENCES gates(gate_id),
    entry_time      TIMESTAMPTZ NOT NULL,
    exit_time       TIMESTAMPTZ,
    session_status  VARCHAR(20) NOT NULL CHECK (session_status IN ('ACTIVE','COMPLETED'))
);
CREATE UNIQUE INDEX uq_active_session_vehicle ON parking_sessions(vehicle_id) WHERE session_status = 'ACTIVE';
CREATE UNIQUE INDEX uq_session_reservation ON parking_sessions(reservation_id);
CREATE INDEX idx_sessions_status ON parking_sessions(session_status);

CREATE TABLE sensors (
    sensor_id      BIGSERIAL PRIMARY KEY,
    device_id      BIGINT      NOT NULL REFERENCES esp32_devices(device_id),
    slot_id        BIGINT      NOT NULL REFERENCES parking_slots(slot_id),
    sensor_type    VARCHAR(30) NOT NULL DEFAULT 'ULTRASONIC',
    sensor_status  VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
                   CHECK (sensor_status IN ('ACTIVE','INACTIVE','FAULT'))
);

CREATE TABLE sensor_readings (
    reading_id          BIGSERIAL PRIMARY KEY,
    sensor_id           BIGINT      NOT NULL REFERENCES sensors(sensor_id),
    distance_cm         NUMERIC(8,2),
    occupancy_detected  BOOLEAN     NOT NULL,
    recorded_at         TIMESTAMPTZ NOT NULL
);
CREATE INDEX idx_readings_sensor_time ON sensor_readings(sensor_id, recorded_at DESC);

CREATE TABLE audit_logs (
    audit_id       BIGSERIAL PRIMARY KEY,
    event_type     VARCHAR(60)  NOT NULL,
    actor_user_id  BIGINT REFERENCES users(user_id),
    actor_device   VARCHAR(80),
    target_type    VARCHAR(40),
    target_id      VARCHAR(60),
    result         VARCHAR(20),
    reason         TEXT,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_audit_created ON audit_logs(created_at DESC);