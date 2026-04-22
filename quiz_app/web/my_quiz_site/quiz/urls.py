from django.urls import path
from . import views

urlpatterns = [
    # localhost:8000/api/ 로 접속했을 때 실행될 뷰
    path('', views.question_list, name='question_list'),
]