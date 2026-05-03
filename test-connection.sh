#!/bin/bash
export PGPASSWORD="Handwork-Pennant-Dizzy2-Powdered-Wireless"
psql "postgresql://postgres:Handwork-Pennant-Dizzy2-Powdered-Wireless@db.pnlsodkhkfwqveivbuak.supabase.co:6543/postgres?sslmode=require" -c "SELECT version();"
