-- MedVerify Reports Schema Migration
-- Version 3.0 - Reports & Signalements

-- Create ENUM type for report types
CREATE TYPE report_type AS ENUM (
    'COUNTERFEIT',
    'QUALITY_ISSUE',
    'EXPIRED',
    'PACKAGING_DEFECT',
    'ADVERSE_REACTION',
    'OTHER'
);

-- Create ENUM type for report status
CREATE TYPE report_status AS ENUM (
    'SUBMITTED',
    'UNDER_REVIEW',
    'CONFIRMED',
    'REJECTED',
    'CLOSED'
);

-- Create reports table
CREATE TABLE reports (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    medication_id UUID REFERENCES medications(id) ON DELETE SET NULL,
    gtin VARCHAR(14),
    serial_number VARCHAR(100),
    report_type report_type NOT NULL,
    description VARCHAR(2000) NOT NULL,
    purchase_location JSONB,
    anonymous BOOLEAN NOT NULL DEFAULT FALSE,
    status report_status NOT NULL DEFAULT 'SUBMITTED',
    reference_number VARCHAR(50) UNIQUE NOT NULL,
    reviewed_by UUID REFERENCES users(id) ON DELETE SET NULL,
    reviewed_at TIMESTAMP WITH TIME ZONE,
    review_notes VARCHAR(1000),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create report_photos table
CREATE TABLE report_photos (
    report_id UUID NOT NULL REFERENCES reports(id) ON DELETE CASCADE,
    photo_url VARCHAR(500) NOT NULL
);

-- Indexes for reports
CREATE INDEX idx_report_user ON reports(user_id);
CREATE INDEX idx_report_medication ON reports(medication_id);
CREATE INDEX idx_report_status ON reports(status);
CREATE UNIQUE INDEX idx_report_reference ON reports(reference_number);
CREATE INDEX idx_report_date ON reports(created_at);
CREATE INDEX idx_report_type ON reports(report_type);

-- Trigger to update updated_at on reports table
CREATE TRIGGER update_reports_updated_at
    BEFORE UPDATE ON reports
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Comments for documentation
COMMENT ON TABLE reports IS 'Reports of suspicious medications';
COMMENT ON COLUMN reports.reference_number IS 'Unique reference number for tracking (format: REP-YYYY-XXXXXX)';
COMMENT ON COLUMN reports.purchase_location IS 'JSON object with purchase location details';
COMMENT ON COLUMN reports.anonymous IS 'Whether the report is anonymous';

