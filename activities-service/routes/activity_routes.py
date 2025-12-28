from flask_restful import Resource
from flask import request, jsonify
from models import db, Activity
from schemas import ActivitySchema
from services.activity_service import ActivityService
from services.notification_service import NotificationService
import os


activity_service = ActivityService(module_service_url=os.getenv('MODULE_SERVICE_URL', 'http://localhost:8083'))
notification_service = NotificationService(
    host=os.getenv('RABBITMQ_HOST', 'localhost'),
    port=int(os.getenv('RABBITMQ_PORT', '5672')),
    user=os.getenv('RABBITMQ_USER', 'guest'),
    password=os.getenv('RABBITMQ_PASSWORD', 'guest'),
    exchange=os.getenv('RABBITMQ_EXCHANGE', 'edupath.exchange'),
    routing_key=os.getenv('RABBITMQ_ROUTING_KEY', 'activity.routing.key')
)
notification_service.connect()

activity_schema = ActivitySchema()


def get_teacher_from_headers():
    """Récupérer les informations de l'enseignant depuis les headers"""
    teacher_id = request.headers.get('X-User-Id', 'teacher-1')
    teacher_username = request.headers.get('X-User-Username', 'teacher')
    return teacher_id, teacher_username


class ActivityListResource(Resource):
    def get(self):
        """Obtenir toutes les activités (filtrées par module si fourni)"""
        module_id = request.args.get('module_id', type=int)
        
        if module_id:
            activities = activity_service.get_activities_by_module(module_id)
        else:
            activities = Activity.query.all()
        
        return jsonify([a.to_dict() for a in activities])
    
    def post(self):
        """Créer une nouvelle activité"""
        teacher_id, teacher_username = get_teacher_from_headers()
        
        try:
            data = activity_schema.load(request.json)
            activity = activity_service.create_activity(data, teacher_id, teacher_username)
            
            # Envoyer une notification
            notification_service.send_activity_notification(activity.to_dict())
            
            return activity.to_dict(), 201
        except Exception as e:
            return {'error': str(e)}, 400


class ActivityResource(Resource):
    def get(self, activity_id):
        """Obtenir une activité par ID"""
        activity = activity_service.get_activity_by_id(activity_id)
        if not activity:
            return {'error': 'Activité non trouvée'}, 404
        return jsonify(activity.to_dict())
    
    def put(self, activity_id):
        """Mettre à jour une activité"""
        teacher_id, _ = get_teacher_from_headers()
        
        try:
            data = activity_schema.load(request.json, partial=True)
            activity = activity_service.update_activity(activity_id, data, teacher_id)
            
            if not activity:
                return {'error': 'Activité non trouvée'}, 404
            
            return jsonify(activity.to_dict())
        except ValueError as e:
            return {'error': str(e)}, 403
        except Exception as e:
            return {'error': str(e)}, 400
    
    def delete(self, activity_id):
        """Supprimer une activité"""
        teacher_id, _ = get_teacher_from_headers()
        
        try:
            success = activity_service.delete_activity(activity_id, teacher_id)
            if not success:
                return {'error': 'Activité non trouvée'}, 404
            return {'message': 'Activité supprimée'}, 200
        except ValueError as e:
            return {'error': str(e)}, 403
        except Exception as e:
            return {'error': str(e)}, 400


class ActivityByModuleResource(Resource):
    def get(self, module_id):
        """Obtenir toutes les activités d'un module"""
        activities = activity_service.get_activities_by_module(module_id)
        return jsonify([a.to_dict() for a in activities])


class ActivityByStudentResource(Resource):
    def get(self, student_id):
        """Obtenir toutes les activités d'un étudiant"""
        module_id = request.args.get('module_id', type=int)
        
        if module_id:
            activities = activity_service.get_activities_by_student_and_module(student_id, module_id)
            stats = activity_service.get_activity_statistics(student_id, module_id)
            return jsonify({
                'activities': [a.to_dict() for a in activities],
                'statistics': stats
            })
        else:
            activities = activity_service.get_activities_by_student(student_id)
            return jsonify([a.to_dict() for a in activities])


