ALTER TABLE learning_initiatives
    DROP CONSTRAINT chk_learning_initiatives_dates;

ALTER TABLE learning_initiatives
    ADD CONSTRAINT chk_learning_initiatives_dates CHECK (expiry_date_utc >= start_date_utc);
