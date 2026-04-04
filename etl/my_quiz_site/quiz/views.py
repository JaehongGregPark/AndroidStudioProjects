# my_quiz_site/quiz/views.py

from rest_framework import viewsets
from .models import Question
from .serializers import QuestionSerializer

class QuestionViewSet(viewsets.ReadOnlyModelViewSet):
    # 'order_by'가 맞습니다. (정렬 기능)
    queryset = Question.objects.all().order_by('number')
    serializer_class = QuestionSerializer