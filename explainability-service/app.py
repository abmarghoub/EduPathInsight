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
    'postgresql://postgres:postgres@localhost:5432/edupath_explainability'
)
app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False
app.config['SECRET_KEY'] = os.getenv('SECRET_KEY', 'your-secret-key-here')
app.config['PREDICTION_SERVICE_URL'] = os.getenv('PREDICTION_SERVICE_URL', 'http://localhost:8001')

# Initialisation
api = Api(app)
CORS(app)

# Import db depuis models (après la configuration)
from models import db
db.init_app(app)

# Import des routes (après l'initialisation de db)
from routes import explanation_routes, report_routes

# Enregistrement des routes
api.add_resource(explanation_routes.ExplanationResource, '/api/explainability/explain/<string:student_id>/<int:module_id>')
api.add_resource(explanation_routes.ExplanationListResource, '/api/explainability/explanations/student/<string:student_id>')
api.add_resource(explanation_routes.FeatureImportanceResource, '/api/explainability/features/<string:student_id>/<int:module_id>')

api.add_resource(report_routes.ReportResource, '/api/explainability/report/<string:student_id>/<int:module_id>')
api.add_resource(report_routes.ReportListResource, '/api/explainability/reports/student/<string:student_id>')
api.add_resource(report_routes.GenerateReportResource, '/api/explainability/reports/generate')

if __name__ == '__main__':
    with app.app_context():
        from models import db
        db.create_all()
    
    port = int(os.getenv('PORT', 5001))
    app.run(debug=True, host='0.0.0.0', port=port)

