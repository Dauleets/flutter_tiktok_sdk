part of '../flutter_tiktok_sdk.dart';

/// manager for TikTok SDK all features
class TikTokSDK {
  static const MethodChannel _channel =
      MethodChannel('com.dauleets/flutter_tiktok_sdk');

  /// singleton object of TikTokSDK
  static final TikTokSDK instance = TikTokSDK._();

  TikTokSDK._();

  /// setup TikTokSDK
  /// Must be called only on Android.
  ///
  /// [clientKey] It is issued when you register your app on TikTok for developers.
  /// https://developers.tiktok.com/
  Future<void> setup({required String clientKey}) async {
    if (Platform.isIOS) {
      return;
    }

    await _channel.invokeMethod(
      'setup',
      <String, dynamic>{
        'clientKey': clientKey,
      },
    );
  }

  void listenToLinks() {
    _channel.setMethodCallHandler((call) async {
      if (call.method == 'onLinkOpened') {
        final link = call.arguments as String;
        print('Received link: $link');
        // Можете обработать ссылку, например, сохранить или отобразить.
      }
    });
  }

  

  /// login TikTok
  ///
  /// [permissionType] You must apply for permissions at the time of app registration.
 Future<TikTokLoginResult> login({
    required Set<TikTokPermissionType> permissions,
    String? state,
  }) async {
    final scope = permissions.map((e) => e.scopeName).join(',');
    final result = await _channel.invokeMapMethod<String, dynamic>(
      'login',
      {'scope': scope, 'state': state},
    );

    if (result != null) {
      final grantedPermissions = (result['grantedPermissions'] as String)
          .split(',')
          .map((e) => TikTokPermissionType.values.firstWhere(
                (perm) => perm.scopeName == e,
                orElse: () => TikTokPermissionType.userInfoBasic,
              ))
          .whereType<TikTokPermissionType>()
          .toSet();

      return TikTokLoginResult(
        status: TikTokLoginStatus.success,
        authCode: result['authCode'],
        state: result['state'],
        grantedPermissions: grantedPermissions,
      );
    } else {
      return TikTokLoginResult(status: TikTokLoginStatus.error);
    }
  }
}