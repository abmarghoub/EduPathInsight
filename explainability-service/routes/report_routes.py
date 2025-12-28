from flask_restful import Resource
from flask import request, jsonify
from services.report_service import ReportService
from models import Report


class ReportResource(Resource):
    def get(self, student_id, module_id):
        """Obtenir un rapport (génère si nécessaire)"""
        try:
            report_type = request.args.get('type', 'DASHBOARD')  # DASHBOARD, MOBILE, DETAILED
            service = ReportService()
            
            if report_type == 'MOBILE':
                report = service.generate_mobile_report(student_id, module_id)
            elif report_type == 'DETAILED':
                report = service.generate_detailed_report(student_id, module_id)
            else:
                report = service.generate_dashboard_report(student_id, module_id)
            
            return jsonify(report)
        except Exception as e:
            return {'error': str(e)}, 500


class ReportListResource(Resource):
    def get(self, student_id):
        """Obtenir tous les rapports d'un étudiant"""
        try:
            report_type = request.args.get('type')
            service = ReportService()
            reports = service.get_reports_by_student(student_id, report_type)
            return jsonify(reports)
        except Exception as e:
            return {'error': str(e)}, 500


class GenerateReportResource(Resource):
    def post(self):
        """Générer un rapport avec paramètres spécifiques"""
        try:
            data = request.json
            student_id = data.get('student_id')
            module_id = data.get('module_id')
            report_type = data.get('type', 'DASHBOARD')
            
            if not student_id or not module_id:
                return {'error': 'student_id et module_id sont requis'}, 400
            
            service = ReportService()
            
            if report_type == 'MOBILE':
                report = service.generate_mobile_report(student_id, module_id)
            elif report_type == 'DETAILED':
                report = service.generate_detailed_report(student_id, module_id)
            else:
                report = service.generate_dashboard_report(student_id, module_id)
            
            return jsonify(report), 201
        except Exception as e:
            return {'error': str(e)}, 500


