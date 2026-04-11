# my_quiz_site/quiz/urls.py
from django.urls import path, include
from rest_framework.routers import DefaultRouter
from . import views

router = DefaultRouter()
router.register(r'questions', views.QuestionViewSet, basename='question')

urlpatterns = [
    # 1. 기존 API 주소 (localhost:8000/api/questions/)
    path('', include(router.urls)),
    
    # 2. 새로 만든 웹 화면 주소 (localhost:8000/api/list/)
    path('list/', views.question_list, name='question_list'),
    path('bulk-edit/<int:exam_id>/', views.exam_bulk_edit, name='exam_bulk_edit'),
    path('admin-image-matcher/', views.admin_image_matcher, name='admin_image_matcher'),
    path('admin-manual/', views.admin_manual, name='admin_manual'),
    path('admin-ocr-update/', views.admin_answer_ocr_update, name='admin_answer_ocr_update'),
    path('quiz/<int:exam_id>/', views.quiz_detail, name='quiz_detail'),
    path('admin/bulk-practical-upload/', views.admin_bulk_practical_upload, name='admin_bulk_practical_upload'),
]