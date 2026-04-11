from django.contrib import admin
from django.utils.html import format_html
from django.urls import reverse
from .models import Category, Exam, Question, Choice, QuestionImage
from django.utils.safestring import mark_safe

# 관리자 페이지 상단 타이틀 변경
admin.site.site_header = "기출문제 통합 관리 시스템 (CMS)"
admin.site.site_title = "운영자 전용"
admin.site.index_title = "환영합니다! 운영 대시보드입니다."

# 관리자 페이지 상단 타이틀 및 링크 추가
admin.site.site_header = "기출문제 관리 시스템"
admin.site.index_title = mark_safe(
    '기본 관리는 아래 목록을 이용하시고, 대량 작업은 '
    '<a href="/api/admin-manual/" style="background:#417690; color:white; padding:5px 10px; border-radius:4px; text-decoration:none;">'
    '🚀 통합 운영 메뉴얼 바로가기</a> 을 이용하세요.'
)

@admin.register(Category)
class CategoryAdmin(admin.ModelAdmin):
    pass

@admin.register(Exam)
class ExamAdmin(admin.ModelAdmin):
    list_display = ['title', 'category', 'created_at','bulk_edit_button']
    list_filter = ['category']
    def bulk_edit_button(self, obj):
        # 방금 만든 URL로 연결되는 버튼 생성
        url = reverse('exam_bulk_edit', args=[obj.id])
        return format_html('<a class="button" href="{}" style="background: #79aec8; color: white; padding: 3px 10px; border-radius: 4px;">🎯 일괄 편집</a>', url)
    
    bulk_edit_button.short_description = "작업 도구"
# 보기를 문제 상세 페이지에서 바로 수정할 수 있게 만듭니다.
class ChoiceInline(admin.TabularInline):
    model = Choice
    extra = 4  # 기본으로 4개의 보기 칸을 보여줍니다.

# 문제 상세 페이지에서 이미지를 바로 올릴 수 있게 만듭니다.
class QuestionImageInline(admin.TabularInline):
    model = QuestionImage
    extra = 1 # 기본으로 1개의 업로드 칸을 보여줌    


@admin.register(Question)
class QuestionAdmin(admin.ModelAdmin):
    # 리스트 화면에서 보여줄 항목들
    list_display = ['number', 'get_exam_title','content_summary', 'answer', 'created_at', 'has_image']
    
    # 우측 사이드바에 콤보박스(필터)를 생성하는 핵심 코드!
    # 이 부분에 'exam__category'를 넣으면 분류별로, 'exam'을 넣으면 시험지별로 필터링 가능합니다.
    list_filter = ['exam__category', 'exam']
   
    # 상세보기 페이지에 보기(Choice) 편집창을 포함시킵니다.
    inlines = [ChoiceInline, QuestionImageInline ]
    # 검색 기능
    search_fields = ['content', 'number']
    ordering = ['number']

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
    
    def has_image(self, obj):
        # 해당 문제(obj)에 연결된 이미지들이 존재하는지 확인
        return obj.images.exists()  
    
    # 시험지 제목을 가져오는 함수
    def get_exam_title(self, obj):
        return obj.exam.title if obj.exam else "미분류"
    get_exam_title.short_description = "시험지"
      
    # 관리자 화면에서 텍스트(True/False) 대신 예쁜 아이콘(✔/✘)으로 보여주는 설정
    has_image.boolean = True 
    has_image.short_description = "이미지 여부"    


# Choice도 따로 관리하고 싶다면 등록 (선택사항)
admin.site.register(Choice)

# 관리자 페이지에서 Question과 그에 딸린 보기, 이미지를 한 번에 관리하게 해줍니다.
#admin.site.register(Question)
#admin.site.register(Choice)
#admin.site.register(QuestionImage)
