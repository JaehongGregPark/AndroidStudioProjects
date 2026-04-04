# my_quiz_site/my_quiz_site/urls.py

from django.contrib import admin
from django.urls import path, include
from rest_framework.routers import DefaultRouter
from quiz.views import QuestionViewSet

# API 주소를 자동으로 만들어주는 도구
router = DefaultRouter()
router.register(r'questions', QuestionViewSet, basename='question')

urlpatterns = [
    path('admin/', admin.site.urls),
    path('api/', include(router.urls)),  # 이 줄이 'api/questions/' 주소를 만들어줍니다.
]