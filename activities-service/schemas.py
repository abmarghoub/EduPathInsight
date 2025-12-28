from marshmallow import Schema, fields, validate, ValidationError
from datetime import date, time


class PresenceSchema(Schema):
    student_id = fields.Str(required=True)
    module_id = fields.Int(required=True)
    session_date = fields.Date(required=True)
    session_time = fields.Time(allow_none=True)
    status = fields.Str(required=True, validate=validate.OneOf(['PRESENT', 'ABSENT', 'LATE', 'EXCUSED']))
    notes = fields.Str(allow_none=True)


class ActivitySchema(Schema):
    student_id = fields.Str(required=True)
    module_id = fields.Int(required=True)
    activity_type = fields.Str(required=True, validate=validate.OneOf([
        'LECTURE', 'PRACTICAL', 'LAB', 'ASSIGNMENT', 'PROJECT', 'EXAM', 'OTHER'
    ]))
    title = fields.Str(required=True)
    description = fields.Str(allow_none=True)
    activity_date = fields.Date(required=True)
    duration_minutes = fields.Int(allow_none=True, validate=validate.Range(min=0))
    completed = fields.Bool(missing=False)
    participation_score = fields.Float(allow_none=True, validate=validate.Range(min=0, max=100))
    notes = fields.Str(allow_none=True)


