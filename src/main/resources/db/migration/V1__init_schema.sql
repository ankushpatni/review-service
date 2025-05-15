CREATE TABLE processed_files (
    id SERIAL PRIMARY KEY,
    file_name VARCHAR(255) NOT NULL,
    processed_date TIMESTAMP NOT NULL,
    status VARCHAR(50) NOT NULL,
    records_processed INT NOT NULL DEFAULT 0,
    records_failed INT NOT NULL DEFAULT 0,
    CONSTRAINT uk_processed_files_file_name UNIQUE (file_name)
);

CREATE TABLE review_providers (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    CONSTRAINT uk_review_providers_name UNIQUE (name)
);

CREATE TABLE hotels (
    id INT PRIMARY KEY,
    name VARCHAR(255),
    CONSTRAINT uk_hotels_id UNIQUE (id)
);

CREATE TABLE reviews (
    id SERIAL PRIMARY KEY,
    provider_review_id BIGINT NOT NULL,
    provider_id INT NOT NULL,
    hotel_id INT NOT NULL,
    rating DECIMAL(3, 1) NOT NULL,
    rating_text VARCHAR(50),
    review_date TIMESTAMP NOT NULL,
    review_title TEXT,
    review_comments TEXT,
    review_positives TEXT,
    review_negatives TEXT,
    reviewer_country VARCHAR(100),
    reviewer_name VARCHAR(255),
    reviewer_type VARCHAR(100),
    room_type VARCHAR(255),
    length_of_stay INT,
    check_in_date VARCHAR(50),
    formatted_rating VARCHAR(20),
    translate_source VARCHAR(10),
    translate_target VARCHAR(10),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_reviews_provider_review_id_provider_id UNIQUE (provider_review_id, provider_id),
    CONSTRAINT fk_reviews_provider_id FOREIGN KEY (provider_id) REFERENCES review_providers(id),
    CONSTRAINT fk_reviews_hotel_id FOREIGN KEY (hotel_id) REFERENCES hotels(id)
);

CREATE TABLE review_responses (
    id SERIAL PRIMARY KEY,
    review_id INT NOT NULL,
    responder_name VARCHAR(255),
    response_date TIMESTAMP,
    response_text TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_review_responses_review_id FOREIGN KEY (review_id) REFERENCES reviews(id)
);

-- Insert default providers
INSERT INTO review_providers (name) VALUES ('Agoda');
INSERT INTO review_providers (name) VALUES ('Booking.com');
INSERT INTO review_providers (name) VALUES ('Expedia'); 