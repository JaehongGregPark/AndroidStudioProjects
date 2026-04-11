from django.db import models

class Category(models.Model):
    name = models.CharField(max_length=100, unique=True) # 예: 정보처리기사 필기

    def __str__(self):
        return self.name

class Exam(models.Model):
    category = models.ForeignKey(Category, on_delete=models.CASCADE, related_name='exams')
    title = models.CharField(max_length=200) # 예: 2019년1회_기사필기_기출문제
    created_at = models.DateTimeField(auto_now_add=True)

    def __str__(self):
        return f"[{self.category.name}] {self.title}"

# 문제 정보를 담는 바구니
class Question(models.Model):
    exam = models.ForeignKey(Exam, on_delete=models.CASCADE, related_name='questions', null=True)
    number = models.IntegerField()        # 문제 번호
    content = models.TextField()          # 문제 지문
    answer = models.TextField(blank=True, null=True) # 정답 (나중에 채울 수도 있음)
    explanation = models.TextField(blank=True, null=True) # 설명
    created_at = models.DateTimeField(auto_now_add=True)

    def __str__(self):
        # exam이 있을 때만 title을 가져오고, 없으면 '미분류'로 표시
        exam_title = self.exam.title if self.exam else "미분류 시험지"
        return f"{exam_title} - {self.number}번"

# 객관식 보기들을 담는 바구니
class Choice(models.Model):
    # 어떤 문제에 딸린 보기인지 연결 (ForeignKey)
    question = models.ForeignKey(Question, on_delete=models.CASCADE, related_name='choices')
    choice_text = models.TextField()      # 보기 내용 (① 모듈화 등)
    # 보기용 이미지 필드 추가
    image_file = models.ImageField(upload_to='choice_images/', null=True, blank=True)
    is_answer = models.BooleanField(default=False) # 정답 여부 저장
    
    def __str__(self):
        return f"{self.question.number}번의 보기: {self.choice_text[:20]}"

# 문제와 연결된 이미지를 담는 바구니
class QuestionImage(models.Model):
    question = models.ForeignKey(Question, on_delete=models.CASCADE, related_name='images')
    image_file = models.ImageField(upload_to='quiz_images/') # 이미지 저장 경로