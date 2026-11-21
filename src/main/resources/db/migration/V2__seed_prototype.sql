INSERT INTO floors (floor_name) VALUES ('Floor 1'), ('Floor 2');

INSERT INTO parking_slots (floor_id, slot_number)
    SELECT floor_id, 'A1' FROM floors WHERE floor_name = 'Floor 1';
INSERT INTO parking_slots (floor_id, slot_number)
    SELECT floor_id, 'A2' FROM floors WHERE floor_name = 'Floor 1';
INSERT INTO parking_slots (floor_id, slot_number)
    SELECT floor_id, 'B1' FROM floors WHERE floor_name = 'Floor 2';
INSERT INTO parking_slots (floor_id, slot_number)
    SELECT floor_id, 'B2' FROM floors WHERE floor_name = 'Floor 2';

INSERT INTO rates (name, price_per_hour, currency)
    VALUES ('Standard demo rate', 2000.00, 'TZS');