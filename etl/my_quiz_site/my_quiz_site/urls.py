# my_quiz_site/my_quiz_site/urls.py

from django.contrib import admin
from django.urls import path, include
from django.shortcuts import redirect
from django.conf import settings
from django.conf.urls.static import static

urlpatterns = [
    path('admin/', admin.site.urls),
    
    # 이제 quiz.urls를 찾을 수 있게 되었으므로 정상 작동합니다!
    path('api/', include('quiz.urls')), 
    
    # 메인 접속 시 리다이렉트
    #path('', lambda r: redirect('/api/questions/')),
    path('', lambda r: redirect('/api/list/')),
]

# 개발 환경에서 미디어 파일을 서비스하기 위한 설정
if settings.DEBUG:
    urlpatterns += static(settings.MEDIA_URL, document_root=settings.MEDIA_ROOT)
    