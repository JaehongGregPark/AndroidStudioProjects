from django.contrib import admin
from django.utils.html import format_html
from .models import Category, Exam, Question, Choice, QuestionImage

# 관리자 페이지 상단 타이틀 변경
admin.site.site_header = "기출문제 통합 관리 시스템 (CMS)"
admin.site.site_title = "운영자 전용"
admin.site.index_title = "환영합니다! 운영 대시보드입니다."

# 문제 상세 페이지에서 이미지를 바로 올릴 수 있게 만듭니다.
class QuestionImageInline(admin.TabularInline):
    model = QuestionImage
    extra = 1 # 기본으로 1개의 업로드 칸을 보여줌

# 보기를 문제 상세 페이지에서 바로 수정할 수 있게 만듭니다.
class ChoiceInline(admin.TabularInline):
    model = Choice
    extra = 4  # 기본으로 4개의 보기 칸을 보여줍니다.

@admin.register(Category)
class CategoryAdmin(admin.ModelAdmin):
    pass

@admin.register(Exam)
class ExamAdmin(admin.ModelAdmin):
    list_display = ['title', 'category', 'created_at']
    list_filter = ['category']

@admin.register(Question)
class QuestionAdmin(admin.ModelAdmin):
    # 리스트 화면에서 보여줄 항목들
    list_display = ['number', 'content_summary', 'has_image']
    list_filter = ['created_at']
    # 상세보기 페이지에 보기(Choice) 편집창을 포함시킵니다.
    inlines = [ChoiceInline, QuestionImageInline]
    # 검색 기능
    search_fields = ['content', 'number']
    ordering = ['number']
    
    def has_image(self, obj):
        return obj.images.exists()
    has_image.boolean = True
    has_image.short_description = "이미지 여부"

    # 지문이 너무 길면 잘라서 보여주는 함수
    def content_summary(self, obj):
        return obj.content[:30] + "..."
    content_summary.short_description = '문제 지문'

    def get_content_preview(self, obj):
        return obj.content[:40] + "..."
    get_content_preview.short_description = "문제 미리보기"

    def has_answer(self, obj):
        # 정답이 입력되었는지 아이콘으로 표시
        if obj.answer:
            return format_html('<span style="color: green;">✔ 입력됨</span>')
        return format_html('<span style="color: red;">✘ 미입력</span>')
    has_answer.short_description = "정답 상태"

# 관리자 페이지에서 Question과 그에 딸린 보기, 이미지를 한 번에 관리하게 해줍니다.
admin.site.register(Choice)
admin.site.register(QuestionImage)

