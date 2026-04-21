import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;
import 'dart:convert';

void main() => runApp(const QuizApp());

class QuizApp extends StatelessWidget {
  const QuizApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: '정보처리기사 문제은행',
      theme: ThemeData(primarySwatch: Colors.blue),
      home: const QuestionListScreen(),
    );
  }
}

class QuestionListScreen extends StatefulWidget {
  const QuestionListScreen({super.key});

  @override
  _QuestionListScreenState createState() => _QuestionListScreenState();
}

class _QuestionListScreenState extends State<QuestionListScreen> {
  // 서버에서 가져온 데이터를 담을 리스트
  List questions = [];

  @override
  void initState() {
    super.did_set_state();
    fetchQuestions(); // 앱 시작 시 데이터 가져오기
  }

  // 장고 서버에 접속해서 JSON 데이터를 가져오는 함수
  Future<void> fetchQuestions() async {
    // 주의: 에뮬레이터에서는 127.0.0.1 대신 10.0.2.2를 사용해야 내 컴퓨터 서버에 접속됩니다.
    final response = await http.get(Uri.parse('http://10.0.2.2:8000/api/questions/'));

    if (response.statusCode == 200) {
      setState(() {
        // 한글 깨짐 방지를 위해 utf8.decode 사용
        questions = json.decode(utf8.decode(response.bodyBytes));
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('정보처리기사 기출문제')),
      body: questions.isEmpty
          ? const Center(child: CircularProgressIndicator()) // 로딩 중 표시
          : ListView.builder(
              itemCount: questions.length,
              item_builder: (context, index) {
                return ListTile(
                  leading: CircleAvatar(child: Text(questions[index]['number'].toString())),
                  title: Text(questions[index]['content'], maxLines: 1, overflow: TextOverflow.ellipsis),
                  subtitle: Text("보기: ${questions[index]['choices'].length}개"),
                  onTap: () {
                    // 클릭 시 상세 페이지로 이동 (나중에 구현)
                  },
                );
              },
            ),
    );
  }
}