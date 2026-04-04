from django.db import models

# 문제 정보를 담는 바구니
class Question(models.Model):
    number = models.IntegerField()        # 문제 번호
    content = models.TextField()         # 문제 지문
    answer = models.TextField(blank=True) # 정답 (나중에 채울 수도 있음)
    created_at = models.DateTimeField(auto_now_add=True)
    explanation = models.TextField(blank=True, null=True) # 설명

    def __str__(self):
        return f"제 {self.number}번 문제"

# 객관식 보기들을 담는 바구니
class Choice(models.Model):
    # 어떤 문제에 딸린 보기인지 연결 (ForeignKey)
    question = models.ForeignKey(Question, on_delete=models.CASCADE, related_name='choices')
    choice_text = models.TextField()      # 보기 내용 (① 모듈화 등)
    is_answer = models.BooleanField(default=False) # 정답 여부 저장
    
    def __str__(self):
        return self.choice_text

# 문제와 연결된 이미지를 담는 바구니
class QuestionImage(models.Model):
    question = models.ForeignKey(Question, on_delete=models.CASCADE, related_name='images')
    image_file = models.ImageField(upload_to='quiz_images/') # 이미지 저장 경로