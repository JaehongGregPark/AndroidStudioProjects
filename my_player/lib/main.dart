import 'package:flutter/material.dart';
import 'package:on_audio_query/on_audio_query.dart';
import 'package:permission_handler/permission_handler.dart';

void main() {
  runApp(const MaterialApp(
    home: LocalMusicList(),
    debugShowCheckedModeBanner: false,
  ));
}

class LocalMusicList extends StatefulWidget {
  const LocalMusicList({super.key});

  @override
  State<LocalMusicList> createState() => _LocalMusicListState();
}

class _LocalMusicListState extends State<LocalMusicList> {
  final OnAudioQuery _audioQuery = OnAudioQuery();
  bool _hasPermission = false;

  @override
  void initState() {
    super.initState();
    checkAndRequestPermissions();
  }

  // 권한 요청 함수 (status 변수 경고 해결)
  void checkAndRequestPermissions() async {
    // 1. 현재 권한 상태 확인 (안드로이드 13 이상은 Permission.audio 사용)
    PermissionStatus audioStatus = await Permission.audio.request();
    PermissionStatus storageStatus = await Permission.storage.request();

    // 2. 둘 중 하나라도 허용되면 권한이 있는 것으로 간주
    if (audioStatus.isGranted || storageStatus.isGranted) {
      setState(() {
        _hasPermission = true;
      });
      debugPrint("권한 허용됨!");
    } else {
      // 3. 만약 거부되었다면 설정창으로 유도하거나 다시 요청
      debugPrint("권한 거부됨. 설정창에서 직접 허용이 필요할 수 있습니다.");
      // 테스트를 위해 강제로 true를 설정해보고 싶다면 아래 주석을 해제하세요.
      // setState(() { _hasPermission = true; }); 
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text("내 폰 음악 목록"),
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh),
            onPressed: () {
              setState(() {}); // 화면을 다시 그리게 함
            },
          )
        ],
      ),
      body: !_hasPermission
          ? Center(
              child: ElevatedButton(
                onPressed: checkAndRequestPermissions,
                child: const Text("저장소 권한 허용하기"),
              ),
            )
          : FutureBuilder<List<SongModel>>(
              future: _audioQuery.querySongs(
                sortType: null,
                // 주의: 2.9.0 버전에서는 ASC_OR_GREATER가 맞습니다. 
                // 만약 그래도 에러가 나면 이 줄을 통째로 지우셔도 됩니다.
                
                uriType: UriType.EXTERNAL,
                ignoreCase: true,
              ),
              builder: (context, item) {
                if (item.data == null) {
                  return const Center(child: CircularProgressIndicator());
                }
                if (item.data!.isEmpty) {
                  return const Center(child: Text("음악 파일이 없습니다."));
                }
                return ListView.builder(
                  itemCount: item.data!.length,
                  itemBuilder: (context, index) {
                    SongModel song = item.data![index];
                    return ListTile(
                      title: Text(song.title, maxLines: 1, overflow: TextOverflow.ellipsis),
                      subtitle: Text(song.artist ?? "알 수 없는 아티스트"),
                      leading: QueryArtworkWidget(
                        id: song.id,
                        type: ArtworkType.AUDIO,
                        nullArtworkWidget: const Icon(Icons.music_note, size: 40),
                      ),
                      onTap: () {
                        ScaffoldMessenger.of(context).showSnackBar(
                          SnackBar(content: Text("${song.title} 선택됨")),
                        );
                      },
                    );
                  },
                );
              },
            ),
    );
  }
}