from rest_framework import viewsets
from .models import Question
from .serializers import QuestionSerializer
from django.shortcuts import render
import json
import os

class QuestionViewSet(viewsets.ReadOnlyModelViewSet):
    # 모든 문제를 가져오되, 번호 순서대로 정렬합니다.
    queryset = Question.objects.all().order_by('number')
    serializer_class = QuestionSerializer

def question_list(request):
    category_id = request.GET.get('category')
    exam_id = request.GET.get('exam')

    categories = Category.objects.all()
    exams = Exam.objects.all()
    
    # 필터링 로직
    questions = Question.objects.all()
    if category_id:
        questions = questions.filter(exam__category_id=category_id)
        exams = exams.filter(category_id=category_id)
    if exam_id:
        questions = questions.filter(exam_id=exam_id)

    return render(request, 'quiz/question_list.html', {
        'questions': questions,
        'categories': categories,
        'exams': exams,
        'selected_category': category_id,
        'selected_exam': exam_id,
    })