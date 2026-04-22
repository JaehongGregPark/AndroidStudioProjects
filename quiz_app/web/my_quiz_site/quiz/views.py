from rest_framework import viewsets
from .models import Question
from .serializers import QuestionSerializer
from django.shortcuts import render, redirect, get_object_or_404
import json
import os
from .models import Exam, Question

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

def exam_bulk_edit(request, exam_id):
    exam = get_object_or_404(Exam, id=exam_id)
    questions = exam.questions.all().order_by('number')

    if request.method == "POST":
        for question in questions:
            # HTML input name을 'answer_{{question.id}}' 형태로 받을 예정
            new_answer = request.POST.get(f'answer_{question.id}')
            new_number = request.POST.get(f'number_{question.id}')
            
            if new_answer is not None:
                question.answer = new_answer
            if new_number is not None:
                question.number = new_number
            question.save()
            
        return redirect('admin:quiz_exam_changelist') # 저장 후 다시 관리자 목록으로

    return render(request, 'admin/quiz/bulk_edit.html', {
        'exam': exam,
        'questions': questions,
    })