-- Widen PII columns to accommodate KMS-encrypted ciphertext (Base64, ~350–500 chars)
ALTER TABLE user_profiles
    MODIFY COLUMN telegram_chat_id VARCHAR(512),
    MODIFY COLUMN whatsapp_number  VARCHAR(512);
