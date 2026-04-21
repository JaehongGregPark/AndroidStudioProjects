from rest_framework import viewsets
from .models import Question
from .serializers import QuestionSerializer

class QuestionViewSet(viewsets.ReadOnlyModelViewSet):
    # 모든 문제를 가져오되, 번호 순서대로 정렬합니다.
    queryset = Question.objects.all().order_by('number')
    serializer_class = QuestionSerializer