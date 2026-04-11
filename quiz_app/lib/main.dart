import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;

void main() {
  runApp(const QuizApp());
}

class QuizApp extends StatelessWidget {
  const QuizApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      title: '정보처리기사 문제은행',
      theme: ThemeData(primarySwatch: Colors.indigo, useMaterial3: true),
      home: const QuestionListScreen(),
    );
  }
}

// --- 1. 문제 리스트 화면 ---
class QuestionListScreen extends StatefulWidget {
  const QuestionListScreen({super.key});

  @override
  State<QuestionListScreen> createState() => _QuestionListScreenState();
}

class _QuestionListScreenState extends State<QuestionListScreen> {
  List questions = [];
  bool isLoading = true;

  @override
  void initState() {
    super.initState();
    fetchQuestions();
  }

  // lib/main.dart 내부 fetchQuestions 함수 수정

  Future<void> fetchQuestions() async {
    try {
      // 실행 환경이 웹(브라우저)인지 체크하여 주소를 변경합니다.
      // 웹이면 localhost, 아니면(에뮬레이터면) 10.0.2.2 사용
      String url = "http://127.0.0.1:8000/api/questions/";

      // 만약 안드로이드 에뮬레이터로 실행 중이라면 아래 주소로 시도
      // url = "http://10.0.2.2:8000/api/questions/";

      final response = await http.get(Uri.parse(url));

      if (response.statusCode == 200) {
        setState(() {
          questions = json.decode(utf8.decode(response.bodyBytes));
          isLoading = false;
        });
      }
    } catch (e) {
      debugPrint("에러 상세: $e");
      setState(() {
        isLoading = false;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('정보처리기사 기출')),
      body: isLoading
          ? const Center(child: CircularProgressIndicator())
          : ListView.separated(
              itemCount: questions.length,
              separatorBuilder: (context, index) => const Divider(),
              itemBuilder: (context, index) {
                return ListTile(
                  leading: CircleAvatar(
                    child: Text("${questions[index]['number']}"),
                  ),
                  title: Text(
                    questions[index]['content'],
                    maxLines: 2,
                    overflow: TextOverflow.ellipsis,
                  ),
                  onTap: () {
                    Navigator.push(
                      context,
                      MaterialPageRoute(
                        builder: (context) =>
                            QuestionDetailScreen(question: questions[index]),
                      ),
                    );
                  },
                );
              },
            ),
    );
  }
}

// --- 2. 문제 상세 및 정답 확인 화면 ---
class QuestionDetailScreen extends StatefulWidget {
  final Map question;

  const QuestionDetailScreen({super.key, required this.question});

  @override
  State<QuestionDetailScreen> createState() => _QuestionDetailScreenState();
}

class _QuestionDetailScreenState extends State<QuestionDetailScreen> {
  int? selectedIndex;
  bool isSubmitted = false;

  void checkAnswer(int index) {
    setState(() {
      selectedIndex = index;
      isSubmitted = true;
    });
  }

  @override
  Widget build(BuildContext context) {
    List choices = widget.question['choices'];

    return Scaffold(
      appBar: AppBar(title: Text('${widget.question['number']}번 문제')),
      body: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              widget.question['content'],
              style: const TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 20),
            Expanded(
              child: ListView.builder(
                itemCount: choices.length,
                itemBuilder: (context, index) {
                  Color cardColor = Colors.white;
                  if (isSubmitted) {
                    if (choices[index]['is_answer'])
                      cardColor = Colors.green[100]!;
                    else if (selectedIndex == index)
                      cardColor = Colors.red[100]!;
                  }

                  return GestureDetector(
                    onTap: () => isSubmitted ? null : checkAnswer(index),
                    child: Card(
                      color: cardColor,
                      elevation: 2,
                      margin: const EdgeInsets.symmetric(vertical: 8),
                      child: Padding(
                        padding: const EdgeInsets.all(16.0),
                        child: Text(
                          "${index + 1}. ${choices[index]['choice_text']}",
                          style: const TextStyle(fontSize: 16),
                        ),
                      ),
                    ),
                  );
                },
              ),
            ),
            if (isSubmitted)
              Padding(
                padding: const EdgeInsets.only(top: 20),
                child: Text(
                  "해설: ${widget.question['explanation'] ?? '해설이 없습니다.'}",
                  style: const TextStyle(color: Colors.blueGrey),
                ),
              ),
          ],
        ),
      ),
    );
  }
}
