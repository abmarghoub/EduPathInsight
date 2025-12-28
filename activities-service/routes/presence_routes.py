from flask_restful import Resource, reqparse
from flask import request, jsonify
from models import db, Presence
from schemas import PresenceSchema
from services.presence_service import PresenceService
from services.notification_service import NotificationService
from datetime import datetime
import os


presence_service = PresenceService(module_service_url=os.getenv('MODULE_SERVICE_URL', 'http://localhost:8083'))
notification_service = NotificationService(
    host=os.getenv('RABBITMQ_HOST', 'localhost'),
    port=int(os.getenv('RABBITMQ_PORT', '5672')),
    user=os.getenv('RABBITMQ_USER', 'guest'),
    password=os.getenv('RABBITMQ_PASSWORD', 'guest'),
    exchange=os.getenv('RABBITMQ_EXCHANGE', 'edupath.exchange'),
    routing_key=os.getenv('RABBITMQ_ROUTING_KEY', 'activity.routing.key')
)
notification_service.connect()

presence_schema = PresenceSchema()


def get_teacher_from_headers():
    """Récupérer les informations de l'enseignant depuis les headers"""
    # Dans un vrai système, cela viendrait du JWT token validé par l'API Gateway
    teacher_id = request.headers.get('X-User-Id', 'teacher-1')
    teacher_username = request.headers.get('X-User-Username', 'teacher')
    return teacher_id, teacher_username


class PresenceListResource(Resource):
    def get(self):
        """Obtenir toutes les présences (filtrées par module si fourni)"""
        module_id = request.args.get('module_id', type=int)
        
        if module_id:
            presences = presence_service.get_presences_by_module(module_id)
        else:
            presences = Presence.query.all()
        
        return jsonify([p.to_dict() for p in presences])
    
    def post(self):
        """Créer une nouvelle présence"""
        teacher_id, teacher_username = get_teacher_from_headers()
        
        try:
            data = presence_schema.load(request.json)
            presence = presence_service.create_presence(data, teacher_id, teacher_username)
            
            # Envoyer une notification
            notification_service.send_presence_notification(presence.to_dict())
            
            return presence.to_dict(), 201
        except Exception as e:
            return {'error': str(e)}, 400


class PresenceResource(Resource):
    def get(self, presence_id):
        """Obtenir une présence par ID"""
        presence = presence_service.get_presence_by_id(presence_id)
        if not presence:
            return {'error': 'Présence non trouvée'}, 404
        return jsonify(presence.to_dict())
    
    def put(self, presence_id):
        """Mettre à jour une présence"""
        teacher_id, _ = get_teacher_from_headers()
        
        try:
            data = presence_schema.load(request.json, partial=True)
            presence = presence_service.update_presence(presence_id, data, teacher_id)
            
            if not presence:
                return {'error': 'Présence non trouvée'}, 404
            
            return jsonify(presence.to_dict())
        except ValueError as e:
            return {'error': str(e)}, 403
        except Exception as e:
            return {'error': str(e)}, 400
    
    def delete(self, presence_id):
        """Supprimer une présence"""
        teacher_id, _ = get_teacher_from_headers()
        
        try:
            success = presence_service.delete_presence(presence_id, teacher_id)
            if not success:
                return {'error': 'Présence non trouvée'}, 404
            return {'message': 'Présence supprimée'}, 200
        except ValueError as e:
            return {'error': str(e)}, 403
        except Exception as e:
            return {'error': str(e)}, 400


class PresenceByModuleResource(Resource):
    def get(self, module_id):
        """Obtenir toutes les présences d'un module"""
        presences = presence_service.get_presences_by_module(module_id)
        return jsonify([p.to_dict() for p in presences])


class PresenceByStudentResource(Resource):
    def get(self, student_id):
        """Obtenir toutes les présences d'un étudiant"""
        module_id = request.args.get('module_id', type=int)
        
        if module_id:
            presences = presence_service.get_presences_by_student_and_module(student_id, module_id)
            stats = presence_service.get_presence_statistics(student_id, module_id)
            return jsonify({
                'presences': [p.to_dict() for p in presences],
                'statistics': stats
            })
        else:
            presences = presence_service.get_presences_by_student(student_id)
            return jsonify([p.to_dict() for p in presences])


