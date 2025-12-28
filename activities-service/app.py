from flask import Flask
from flask_restful import Api
from flask_cors import CORS
import os
from dotenv import load_dotenv

load_dotenv()

app = Flask(__name__)

# Configuration
app.config['SQLALCHEMY_DATABASE_URI'] = os.getenv(
    'DATABASE_URL', 
    'postgresql://postgres:postgres@localhost:5432/edupath_activities'
)
app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False
app.config['SECRET_KEY'] = os.getenv('SECRET_KEY', 'your-secret-key-here')
app.config['MONGODB_URI'] = os.getenv('MONGODB_URI', 'mongodb://localhost:27017/edupath_activities')
app.config['RABBITMQ_HOST'] = os.getenv('RABBITMQ_HOST', 'localhost')
app.config['RABBITMQ_PORT'] = int(os.getenv('RABBITMQ_PORT', '5672'))
app.config['RABBITMQ_USER'] = os.getenv('RABBITMQ_USER', 'guest')
app.config['RABBITMQ_PASSWORD'] = os.getenv('RABBITMQ_PASSWORD', 'guest')
app.config['DATA_INGESTION_SERVICE_URL'] = os.getenv('DATA_INGESTION_SERVICE_URL', 'http://localhost:8082')
app.config['MODULE_SERVICE_URL'] = os.getenv('MODULE_SERVICE_URL', 'http://localhost:8083')
app.config['AUTH_SERVICE_URL'] = os.getenv('AUTH_SERVICE_URL', 'http://localhost:8081')

# Initialisation
api = Api(app)
CORS(app)

# Import db depuis models (après la configuration)
from models import db
db.init_app(app)

# Import des routes (après l'initialisation de db)
from routes import presence_routes, activity_routes, import_export_routes, anomaly_routes

# Enregistrement des routes
api.add_resource(presence_routes.PresenceListResource, '/api/activities/admin/presences')
api.add_resource(presence_routes.PresenceResource, '/api/activities/admin/presences/<int:presence_id>')
api.add_resource(presence_routes.PresenceByModuleResource, '/api/activities/admin/presences/module/<int:module_id>')
api.add_resource(presence_routes.PresenceByStudentResource, '/api/activities/admin/presences/student/<string:student_id>')

api.add_resource(activity_routes.ActivityListResource, '/api/activities/admin/activities')
api.add_resource(activity_routes.ActivityResource, '/api/activities/admin/activities/<int:activity_id>')
api.add_resource(activity_routes.ActivityByModuleResource, '/api/activities/admin/activities/module/<int:module_id>')
api.add_resource(activity_routes.ActivityByStudentResource, '/api/activities/admin/activities/student/<string:student_id>')

api.add_resource(import_export_routes.ImportPresenceResource, '/api/activities/admin/presences/import')
api.add_resource(import_export_routes.ImportActivityResource, '/api/activities/admin/activities/import')
api.add_resource(import_export_routes.ExportPresenceTemplateResource, '/api/activities/admin/presences/export/template')
api.add_resource(import_export_routes.ExportPresenceTemplateExcelResource, '/api/activities/admin/presences/export/template/excel')
api.add_resource(import_export_routes.ExportPresenceResource, '/api/activities/admin/presences/export/csv')
api.add_resource(import_export_routes.ExportPresenceExcelResource, '/api/activities/admin/presences/export/excel')
api.add_resource(import_export_routes.ExportActivityResource, '/api/activities/admin/activities/export/csv')
api.add_resource(import_export_routes.ExportActivityExcelResource, '/api/activities/admin/activities/export/excel')

api.add_resource(anomaly_routes.AnomalyListResource, '/api/activities/admin/anomalies')
api.add_resource(anomaly_routes.AnomalyResource, '/api/activities/admin/anomalies/<int:anomaly_id>')
api.add_resource(anomaly_routes.AnomalyByStudentResource, '/api/activities/admin/anomalies/student/<string:student_id>')
api.add_resource(anomaly_routes.CheckAnomaliesResource, '/api/activities/admin/anomalies/check')

if __name__ == '__main__':
    with app.app_context():
        db.create_all()
    
    port = int(os.getenv('PORT', 5000))
    app.run(debug=True, host='0.0.0.0', port=port)

