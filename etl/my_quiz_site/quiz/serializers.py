from rest_framework import serializers
from .models import Question, Choice

class ChoiceSerializer(serializers.ModelSerializer):
    class Meta:
        model = Choice
        fields = ['id', 'choice_text', 'is_answer']

class QuestionSerializer(serializers.ModelSerializer):
    # 질문에 딸린 보기(choices)들을 함께 묶어서 보냅니다.
    choices = ChoiceSerializer(many=True, read_only=True)

    class Meta:
        model = Question
        fields = ['id', 'number', 'content', 'explanation', 'choices']