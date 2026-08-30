// This is a basic Flutter widget test.
//
// To perform an interaction with a widget in your test, use the WidgetTester
// utility in the flutter_test package. For example, you can send tap and scroll
// gestures. You can also use WidgetTester to find child widgets in the widget
// tree, read text, and verify that the values of widget properties are correct.

import 'package:flutter_test/flutter_test.dart';

import 'package:method_channel_test/main.dart';

void main() {
  testWidgets('MethodChannel app loads', (WidgetTester tester) async {
    await tester.pumpWidget(const MethodChannelDemoApp());

    expect(find.text('iOS MethodChannel Demo'), findsOneWidget);
    expect(find.text('A real-world pattern in mobile apps: Flutter requests native iOS features through one channel.'), findsOneWidget);
  });
}
