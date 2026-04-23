from django.urls import path
from . import views

urlpatterns = [
    # localhost:8000/api/ 로 접속했을 때 실행될 뷰
    path('', views.question_list, name='question_list'),
    path('bulk-edit/<int:exam_id>/', views.exam_bulk_edit, name='exam_bulk_edit'),
    path('admin-image-matcher/', views.admin_image_matcher, name='admin_image_matcher'),
    path('admin-manual/', views.admin_manual, name='admin_manual'),
]