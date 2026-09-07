ALTER TABLE jobs ADD COLUMN job_role VARCHAR(30);

CREATE INDEX idx_jobs_job_role ON jobs (job_role);
