from flask_restful import Resource
from flask import request, jsonify, send_file
from io import BytesIO
import pandas as pd
import requests
import os
from datetime import datetime, date
from models import Presence, Activity


class ImportPresenceResource(Resource):
    def post(self):
        """Importer des présences via Data Ingestion Service"""
        if 'file' not in request.files:
            return {'error': 'Aucun fichier fourni'}, 400
        
        file = request.files['file']
        async_mode = request.form.get('async', 'false').lower() == 'true'
        
        if file.filename == '':
            return {'error': 'Fichier vide'}, 400
        
        try:
            # Envoyer le fichier à Data Ingestion Service
            data_ingestion_url = os.getenv('DATA_INGESTION_SERVICE_URL', 'http://localhost:8082')
            files = {'file': (file.filename, file.stream, file.content_type)}
            data = {
                'entityType': 'Presence',
                'async': str(async_mode).lower()
            }
            
            response = requests.post(
                f"{data_ingestion_url}/api/ingestion/upload",
                files=files,
                data=data,
                timeout=30
            )
            
            if response.status_code == 200:
                return response.json(), 200
            else:
                return {'error': f'Erreur lors de l\'import: {response.text}'}, response.status_code
        except Exception as e:
            return {'error': str(e)}, 500


class ImportActivityResource(Resource):
    def post(self):
        """Importer des activités via Data Ingestion Service"""
        if 'file' not in request.files:
            return {'error': 'Aucun fichier fourni'}, 400
        
        file = request.files['file']
        async_mode = request.form.get('async', 'false').lower() == 'true'
        
        if file.filename == '':
            return {'error': 'Fichier vide'}, 400
        
        try:
            # Envoyer le fichier à Data Ingestion Service
            data_ingestion_url = os.getenv('DATA_INGESTION_SERVICE_URL', 'http://localhost:8082')
            files = {'file': (file.filename, file.stream, file.content_type)}
            data = {
                'entityType': 'Activity',
                'async': str(async_mode).lower()
            }
            
            response = requests.post(
                f"{data_ingestion_url}/api/ingestion/upload",
                files=files,
                data=data,
                timeout=30
            )
            
            if response.status_code == 200:
                return response.json(), 200
            else:
                return {'error': f'Erreur lors de l\'import: {response.text}'}, response.status_code
        except Exception as e:
            return {'error': str(e)}, 500


class ExportPresenceTemplateResource(Resource):
    def post(self):
        """Exporter un template de présences pour un module (avec étudiants inscrits et champs vides)"""
        data = request.json
        
        if not data or 'module_id' not in data:
            return {'error': 'module_id est requis'}, 400
        
        module_id = data['module_id']
        session_date = data.get('session_date', date.today().isoformat())
        session_time = data.get('session_time', None)
        
        try:
            # Récupérer les étudiants inscrits au module depuis module-service
            module_service_url = os.getenv('MODULE_SERVICE_URL', 'http://localhost:8083')
            response = requests.get(
                f"{module_service_url}/api/modules/admin/modules/{module_id}/enrollments",
                timeout=5
            )
            
            if response.status_code != 200:
                return {'error': 'Impossible de récupérer les étudiants inscrits'}, 400
            
            enrollments = response.json()
            
            # Filtrer seulement les inscriptions approuvées
            approved_enrollments = [e for e in enrollments if e.get('status') == 'APPROVED']
            
            if not approved_enrollments:
                return {'error': 'Aucun étudiant inscrit et approuvé dans ce module'}, 400
            
            # Récupérer les informations du module
            module_response = requests.get(
                f"{module_service_url}/api/modules/admin/modules/{module_id}",
                timeout=5
            )
            module_info = module_response.json() if module_response.status_code == 200 else {}
            
            # Créer un DataFrame avec les étudiants et champs vides pour remplir
            template_data = []
            for enrollment in approved_enrollments:
                template_data.append({
                    'student_id': enrollment.get('studentId', ''),
                    'student_username': enrollment.get('studentUsername', ''),
                    'student_email': enrollment.get('studentEmail', ''),
                    'module_id': module_id,
                    'module_code': module_info.get('code', ''),
                    'module_name': module_info.get('name', ''),
                    'session_date': session_date,
                    'session_time': session_time if session_time else '',
                    'status': '',  # Vide à remplir: PRESENT, ABSENT, LATE, EXCUSED
                    'notes': ''  # Vide à remplir
                })
            
            df = pd.DataFrame(template_data)
            
            # Convertir en CSV
            output = BytesIO()
            df.to_csv(output, index=False, encoding='utf-8')
            output.seek(0)
            
            filename = f"presences_template_{module_info.get('code', module_id)}_{datetime.now().strftime('%Y%m%d_%H%M%S')}.csv"
            return send_file(output, mimetype='text/csv', as_attachment=True, download_name=filename)
        except Exception as e:
            return {'error': str(e)}, 500


class ExportPresenceTemplateExcelResource(Resource):
    def post(self):
        """Exporter un template de présences pour un module en Excel (avec étudiants inscrits et champs vides)"""
        data = request.json
        
        if not data or 'module_id' not in data:
            return {'error': 'module_id est requis'}, 400
        
        module_id = data['module_id']
        session_date = data.get('session_date', date.today().isoformat())
        session_time = data.get('session_time', None)
        
        try:
            # Récupérer les étudiants inscrits au module depuis module-service
            module_service_url = os.getenv('MODULE_SERVICE_URL', 'http://localhost:8083')
            response = requests.get(
                f"{module_service_url}/api/modules/admin/modules/{module_id}/enrollments",
                timeout=5
            )
            
            if response.status_code != 200:
                return {'error': 'Impossible de récupérer les étudiants inscrits'}, 400
            
            enrollments = response.json()
            
            # Filtrer seulement les inscriptions approuvées
            approved_enrollments = [e for e in enrollments if e.get('status') == 'APPROVED']
            
            if not approved_enrollments:
                return {'error': 'Aucun étudiant inscrit et approuvé dans ce module'}, 400
            
            # Récupérer les informations du module
            module_response = requests.get(
                f"{module_service_url}/api/modules/admin/modules/{module_id}",
                timeout=5
            )
            module_info = module_response.json() if module_response.status_code == 200 else {}
            
            # Créer un DataFrame avec les étudiants et champs vides pour remplir
            template_data = []
            for enrollment in approved_enrollments:
                template_data.append({
                    'student_id': enrollment.get('studentId', ''),
                    'student_username': enrollment.get('studentUsername', ''),
                    'student_email': enrollment.get('studentEmail', ''),
                    'module_id': module_id,
                    'module_code': module_info.get('code', ''),
                    'module_name': module_info.get('name', ''),
                    'session_date': session_date,
                    'session_time': session_time if session_time else '',
                    'status': '',  # Vide à remplir: PRESENT, ABSENT, LATE, EXCUSED
                    'notes': ''  # Vide à remplir
                })
            
            df = pd.DataFrame(template_data)
            
            # Convertir en Excel
            output = BytesIO()
            with pd.ExcelWriter(output, engine='openpyxl') as writer:
                df.to_excel(writer, index=False, sheet_name='Presences Template')
            output.seek(0)
            
            filename = f"presences_template_{module_info.get('code', module_id)}_{datetime.now().strftime('%Y%m%d_%H%M%S')}.xlsx"
            return send_file(output, 
                           mimetype='application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
                           as_attachment=True, download_name=filename)
        except Exception as e:
            return {'error': str(e)}, 500


class ExportPresenceResource(Resource):
    def get(self):
        """Exporter les présences existantes en CSV"""
        module_id = request.args.get('module_id', type=int)
        
        if module_id:
            presences = Presence.query.filter_by(module_id=module_id).all()
        else:
            presences = Presence.query.all()
        
        # Créer un DataFrame
        data = [p.to_dict() for p in presences]
        df = pd.DataFrame(data)
        
        # Convertir en CSV
        output = BytesIO()
        df.to_csv(output, index=False, encoding='utf-8')
        output.seek(0)
        
        filename = f"presences_export_{datetime.now().strftime('%Y%m%d_%H%M%S')}.csv"
        return send_file(output, mimetype='text/csv', as_attachment=True, download_name=filename)


class ExportPresenceExcelResource(Resource):
    def get(self):
        """Exporter les présences existantes en Excel"""
        module_id = request.args.get('module_id', type=int)
        
        if module_id:
            presences = Presence.query.filter_by(module_id=module_id).all()
        else:
            presences = Presence.query.all()
        
        # Créer un DataFrame
        data = [p.to_dict() for p in presences]
        df = pd.DataFrame(data)
        
        # Convertir en Excel
        output = BytesIO()
        with pd.ExcelWriter(output, engine='openpyxl') as writer:
            df.to_excel(writer, index=False, sheet_name='Presences')
        output.seek(0)
        
        filename = f"presences_export_{datetime.now().strftime('%Y%m%d_%H%M%S')}.xlsx"
        return send_file(output, mimetype='application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
                        as_attachment=True, download_name=filename)


class ExportActivityResource(Resource):
    def get(self):
        """Exporter les activités en CSV"""
        module_id = request.args.get('module_id', type=int)
        
        if module_id:
            activities = Activity.query.filter_by(module_id=module_id).all()
        else:
            activities = Activity.query.all()
        
        # Créer un DataFrame
        data = [a.to_dict() for a in activities]
        df = pd.DataFrame(data)
        
        # Convertir en CSV
        output = BytesIO()
        df.to_csv(output, index=False, encoding='utf-8')
        output.seek(0)
        
        filename = f"activities_export_{datetime.now().strftime('%Y%m%d_%H%M%S')}.csv"
        return send_file(output, mimetype='text/csv', as_attachment=True, download_name=filename)


class ExportActivityExcelResource(Resource):
    def get(self):
        """Exporter les activités en Excel"""
        module_id = request.args.get('module_id', type=int)
        
        if module_id:
            activities = Activity.query.filter_by(module_id=module_id).all()
        else:
            activities = Activity.query.all()
        
        # Créer un DataFrame
        data = [a.to_dict() for a in activities]
        df = pd.DataFrame(data)
        
        # Convertir en Excel
        output = BytesIO()
        with pd.ExcelWriter(output, engine='openpyxl') as writer:
            df.to_excel(writer, index=False, sheet_name='Activities')
        output.seek(0)
        
        filename = f"activities_export_{datetime.now().strftime('%Y%m%d_%H%M%S')}.xlsx"
        return send_file(output, mimetype='application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
                        as_attachment=True, download_name=filename)
