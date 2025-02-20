CREATE TABLE users (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    name text NOT NULL,
    email text NOT NULL,
    email_verified boolean DEFAULT false NOT NULL,
    avatar_key text,
    role text DEFAULT 'user'::text NOT NULL,
    score integer DEFAULT 0 NOT NULL,
    level integer DEFAULT 1 NOT NULL,
    currency integer DEFAULT 0 NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL
);

CREATE TABLE password_accounts (
    user_id uuid NOT NULL,
    password text NOT NULL
);

ALTER TABLE ONLY password_accounts
    ADD CONSTRAINT password_accounts_pkey PRIMARY KEY (user_id);