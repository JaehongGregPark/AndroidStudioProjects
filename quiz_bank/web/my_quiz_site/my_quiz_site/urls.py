"""
URL configuration for my_quiz_site project.

The `urlpatterns` list routes URLs to views. For more information please see:
    https://docs.djangoproject.com/en/6.0/topics/http/urls/
Examples:
Function views
    1. Add an import:  from my_app import views
    2. Add a URL to urlpatterns:  path('', views.home, name='home')
Class-based views
    1. Add an import:  from other_app.views import Home
    2. Add a URL to urlpatterns:  path('', Home.as_view(), name='home')
Including another URLconf
    1. Import the include() function: from django.urls import include, path
    2. Add a URL to urlpatterns:  path('blog/', include('blog.urls'))
"""
from django.contrib import admin
from django.urls import path, include
from django.conf import settings
from django.conf.urls.static import static
from rest_framework.routers import DefaultRouter
from django.shortcuts import redirect # 리다이렉트 함수 추가
from quiz.views import QuestionViewSet

router = DefaultRouter()
router.register(r'questions', QuestionViewSet,  basename='question')

urlpatterns = [
    path('admin/', admin.site.urls),
    path('api/', include('quiz.urls')), # http://127.0.0.1:8000/api/questions/ 주소 생성
    # 추가: 아무것도 입력 안 하고 접속했을 때(localhost:8000/) 퀴즈 리스트로 이동
    path('', lambda r: redirect('api/list/')),
]

# 개발 환경에서 미디어 파일을 서비스하기 위한 설정
if settings.DEBUG:
    urlpatterns += static(settings.MEDIA_URL, document_root=settings.MEDIA_ROOT)