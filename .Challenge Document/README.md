Photo mosaic
------------

The goal of this task is to implement the following flow in an android app.
This task should take around 3-5 hours, there is no hard limit,
   focus on getting the best results in this time.

1. A user selects a local image file. -> 로컬 이미지를 선택
2. The app must:
   * load that image; 
     이미지를 불러온다.
   * divide the image into tiles;
     타일로 조각낸다.
   * find the average color for each tile;
     각 타일 별로 평균 컬러를 찾는다.
   * fetch a tile from the provided server (see below) for that color;
     제공된 서버 (아래 참조)에서 해당 색상의 타일을 가져온다.
   * composite the results into a photomosaic of the original image;
     결과를 "원본" 이미지의 포토 모자이크로 합성한다.
     
3. The composited photomosaic should be displayed according to the following
   constraints:
   합성된 포토 모자이크는 다음의 제약에 따라 표시 되어야 한다.
   * tiles should be rendered a complete row at a time (a user should never
      see a row with some completed tiles and some incomplete)
      타일은 한 행마다 한번에 모두 나와야한다.(완전한 한 줄)
   * the mosaic should be rendered from the top row to the bottom row.
     모자이크는 위에서 아래 행 순으로 렌더링 되어야한다.
4. The client app should make effective use of parallelism and asynchrony.

The servers directory contains a simple local mosaic tile server. See the
README file in that directory for more information.

## Constraints

You should, in priority order:
우선적으로 알아둬야 할 사항

 * do not use third-party libraries that directly solve the task,
   this will not tell us anything about you,
   and will not produce adaptable code;
   서드 파티 라이브러리 사용하지 말 것
 * use a tile size of 32x32;
   32 X 32 사이즈
 * make the UI work on different screen sizes;
   UI는 여러 사이즈에서 작동해야한다.
 * use an API level for which source code is available.
   소스코드가 있는 API레벨을 사용하라.
   

You may:
다음과 같은 행동은 할 수 있다.

 * choose a minimum API level you want to support
   지원가능한 최소한의 API 레벨을 선택
 * make it work with different tile sizes other than 32x32;
   32x32 사이즈가 아닌 다른 사이즈에서도 작동
 * be as creative as you like with the submission UI
   UI는 알아서 맘 껏.
   however, it is not the focus of the task, a minimal UI is fine;
   최소한의 UI도 허용
 * use popular libraries like Guava or RxJava if you are familiar with them
   Guava나 RxJava등의 유명 라이브러리 사용 가능
   (be prepared to refactor and extend your code in an interview);
   (면접 시 코드 리팩토링과 코드 추가를 준비)

## Marking Criteria

Your code should be clear and easy to understand:
너의 코드는 명료하고 이해하기 쉬워야 한다.

 * Avoids unnecessary complexity / over-engineering
   불필요한 복잡도 및 오버 엔지니어링 피하라.
 * Brief comments are added where appropriate
   명료한 주석
 * Broken into logical chunks
   논리적 청크로 쪼개라

Your code should be performant:
너의 코드는 다음과 같이 작동해야 한다.

 * Gives feedback to the user as soon as possible (perceived performance)
   사용자에게 피드백을 제공(딜레이 없이)
 * Intelligently coordinates dependent asynchronous tasks
   종속적인 비동기 태스크는 지능적으로 조정하라.
 * UI remains responsive
   반응형 UI

Your code should be testable (but writing tests isn't necessary), for example:
테스트 가능한 코드로 작성 가령,
 * Use dependency injection (the design pattern, not a framework or library)
   의존성 주입 사용
 * Separate presentation logic from business logic.
   비즈니스 로직과 프레젠테이션 로직 분리(MVP)

Have fun!
