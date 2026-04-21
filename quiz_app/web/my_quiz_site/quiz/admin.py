from django.contrib import admin
from .models import Question, Choice, QuestionImage

# 관리자 페이지에서 Question과 그에 딸린 보기, 이미지를 한 번에 관리하게 해줍니다.
admin.site.register(Question)
admin.site.register(Choice)
admin.site.register(QuestionImage)

