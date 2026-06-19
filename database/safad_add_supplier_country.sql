USE safad_db;

ALTER TABLE Supplier
ADD COLUMN country VARCHAR(50) AFTER city;

