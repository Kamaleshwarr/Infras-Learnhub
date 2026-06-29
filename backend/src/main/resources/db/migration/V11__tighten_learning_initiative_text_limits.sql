UPDATE learning_initiatives
SET title = LEFT(title, 100)
WHERE char_length(title) > 100;

ALTER TABLE learning_initiatives
    ALTER COLUMN title TYPE VARCHAR(100);
