# 푸리에 변환 (Fourier Transform)과 고속 푸리에 변환 (Fast Fourier Transform)

푸리에 변환은 시간 도메인의 신호를 주파수 도메인으로 변환하는 수학적 변환입니다. 이 변환은 신호 처리, 이미지 처리, 데이터 압축 등 다양한 분야에서 중요한 역할을 합니다. 고속 푸리에 변환(FFT)은 이러한 푸리에 변환을 효율적으로 계산하기 위한 알고리즘입니다.

## 1. 기본 개념

### 1.1 푸리에 변환의 원리

푸리에 변환의 핵심 아이디어는 어떠한 주기적 신호도 다양한 주파수의 사인파와 코사인파의 합으로 표현할 수 있다는 것입니다. 이를 수학적으로 표현하면 다음과 같습니다:

연속 푸리에 변환(Continuous Fourier Transform):
$$X(f) = \int_{-\infty}^{\infty} x(t) e^{-j2\pi ft} dt$$

이산 푸리에 변환(Discrete Fourier Transform, DFT):
$$X[k] = \sum_{n=0}^{N-1} x[n] e^{-j2\pi kn/N}$$

여기서:
- $x(t)$ 또는 $x[n]$은 시간 도메인의 신호입니다.
- $X(f)$ 또는 $X[k]$는 주파수 도메인의 신호입니다.
- $N$은 샘플의 수입니다.
- $e^{-j2\pi ft}$ 또는 $e^{-j2\pi kn/N}$은 복소 지수 함수입니다.

### 1.2 푸리에 변환의 종류

1. **연속 푸리에 변환(CFT)**: 연속적인 시간 신호에 대한 변환
2. **이산 푸리에 변환(DFT)**: 이산적인(샘플링된) 신호에 대한 변환
3. **고속 푸리에 변환(FFT)**: DFT를 효율적으로 계산하기 위한 알고리즘

## 2. 이산 푸리에 변환(DFT)의 Java 구현

DFT는 디지털 신호 처리에서 가장 기본적인 형태의 푸리에 변환입니다. 다음은 Java로 구현한 DFT 코드입니다:

```java
/**
 * 이산 푸리에 변환(DFT)을 계산하는 클래스
 */
public class DiscreteFourierTransform {
    /**
     * 이산 푸리에 변환을 수행하는 메소드
     * @param x 입력 신호 (실수부)
     * @return 주파수 도메인의 신호 (복소수 배열, [실수부, 허수부] 쌍으로 구성)
     */
    public static double[][] dft(double[] x) {
        int n = x.length;
        double[][] result = new double[n][2]; // [실수부, 허수부]

        for (int k = 0; k < n; k++) {
            double realSum = 0;
            double imagSum = 0;

            for (int t = 0; t < n; t++) {
                // 오일러 공식: e^(-j2πkt/n) = cos(2πkt/n) - j·sin(2πkt/n)
                double angle = 2 * Math.PI * k * t / n;
                realSum += x[t] * Math.cos(angle);
                imagSum -= x[t] * Math.sin(angle);
            }

            result[k][0] = realSum;
            result[k][1] = imagSum;
        }

        return result;
    }

    /**
     * 역 이산 푸리에 변환을 수행하는 메소드
     * @param X 주파수 도메인의 신호 (복소수 배열)
     * @return 시간 도메인의 신호 (실수부)
     */
    public static double[] idft(double[][] X) {
        int n = X.length;
        double[] result = new double[n];

        for (int t = 0; t < n; t++) {
            double sum = 0;

            for (int k = 0; k < n; k++) {
                double angle = 2 * Math.PI * k * t / n;
                double real = X[k][0];
                double imag = X[k][1];

                // 오일러 공식: e^(j2πkt/n) = cos(2πkt/n) + j·sin(2πkt/n)
                sum += real * Math.cos(angle) - imag * Math.sin(angle);
            }

            // 정규화
            result[t] = sum / n;
        }

        return result;
    }

    /**
     * DFT 사용 예시
     */
    public static void main(String[] args) {
        // 예제 신호: 사인파 + 코사인파
        int n = 64; // 샘플 수
        double[] signal = new double[n];

        for (int i = 0; i < n; i++) {
            double t = i / (double) n;
            // 10Hz 사인파 + 20Hz 코사인파
            signal[i] = Math.sin(2 * Math.PI * 10 * t) + 0.5 * Math.cos(2 * Math.PI * 20 * t);
        }

        // DFT 수행
        double[][] spectrum = dft(signal);

        // 주파수 스펙트럼의 크기 계산 및 출력
        System.out.println("주파수 스펙트럼 (크기):");
        for (int i = 0; i < n/2; i++) { // 나이퀴스트 주파수까지만 출력
            double magnitude = Math.sqrt(spectrum[i][0] * spectrum[i][0] + spectrum[i][1] * spectrum[i][1]);
            System.out.printf("주파수 %d Hz: %.4f%n", i, magnitude);
        }

        // 역변환 수행
        double[] reconstructed = idft(spectrum);

        // 원본 신호와 역변환 결과 비교
        System.out.println("\n원본 신호와 역변환 결과 비교 (처음 10개 샘플):");
        for (int i = 0; i < 10; i++) {
            System.out.printf("샘플 %d: 원본=%.4f, 복원=%.4f%n", i, signal[i], reconstructed[i]);
        }
    }
}
```

## 3. 고속 푸리에 변환(FFT)의 Java 구현

고속 푸리에 변환(FFT)은 DFT를 효율적으로 계산하기 위한 알고리즘으로, 분할 정복(divide and conquer) 전략을 사용합니다. 가장 널리 알려진 FFT 알고리즘은 쿨리-튜키(Cooley-Tukey) 알고리즘입니다.

```java
/**
 * 고속 푸리에 변환(FFT)을 계산하는 클래스
 */
public class FastFourierTransform {
    /**
     * 고속 푸리에 변환을 수행하는 메소드
     * @param x 입력 신호 (복소수 배열, [실수부, 허수부] 쌍으로 구성)
     * @return 주파수 도메인의 신호 (복소수 배열)
     */
    public static double[][] fft(double[][] x) {
        int n = x.length;

        // 기저 사례: 신호의 길이가 1인 경우
        if (n == 1) {
            return x;
        }

        // 입력 신호의 길이가 2의 거듭제곱인지 확인
        if (n % 2 != 0) {
            throw new IllegalArgumentException("FFT 입력 길이는 2의 거듭제곱이어야 합니다.");
        }

        // 짝수 인덱스와 홀수 인덱스로 신호 분할
        double[][] even = new double[n/2][2];
        double[][] odd = new double[n/2][2];

        for (int i = 0; i < n/2; i++) {
            even[i][0] = x[2*i][0];
            even[i][1] = x[2*i][1];
            odd[i][0] = x[2*i+1][0];
            odd[i][1] = x[2*i+1][1];
        }

        // 분할된 신호에 대해 재귀적으로 FFT 수행
        double[][] evenResult = fft(even);
        double[][] oddResult = fft(odd);

        // 결과 병합
        double[][] result = new double[n][2];

        for (int k = 0; k < n/2; k++) {
            // 트위들 팩터(twiddle factor) 계산: e^(-j2πk/n)
            double angle = -2 * Math.PI * k / n;
            double twiddle_real = Math.cos(angle);
            double twiddle_imag = Math.sin(angle);

            // 홀수 부분에 트위들 팩터 적용
            double odd_real = oddResult[k][0] * twiddle_real - oddResult[k][1] * twiddle_imag;
            double odd_imag = oddResult[k][0] * twiddle_imag + oddResult[k][1] * twiddle_real;

            // 짝수 부분과 홀수 부분 결합
            result[k][0] = evenResult[k][0] + odd_real;
            result[k][1] = evenResult[k][1] + odd_imag;

            result[k + n/2][0] = evenResult[k][0] - odd_real;
            result[k + n/2][1] = evenResult[k][1] - odd_imag;
        }

        return result;
    }

    /**
     * 역 고속 푸리에 변환을 수행하는 메소드
     * @param X 주파수 도메인의 신호 (복소수 배열)
     * @return 시간 도메인의 신호 (복소수 배열)
     */
    public static double[][] ifft(double[][] X) {
        int n = X.length;

        // 켤레 복소수 계산
        double[][] conjugated = new double[n][2];
        for (int i = 0; i < n; i++) {
            conjugated[i][0] = X[i][0];
            conjugated[i][1] = -X[i][1];
        }

        // FFT 수행
        double[][] result = fft(conjugated);

        // 정규화 및 켤레 복소수 계산
        for (int i = 0; i < n; i++) {
            result[i][0] = result[i][0] / n;
            result[i][1] = -result[i][1] / n;
        }

        return result;
    }

    /**
     * 실수 신호에 대한 FFT 수행 (편의 메소드)
     * @param x 실수 신호
     * @return 주파수 도메인의 신호 (복소수 배열)
     */
    public static double[][] fft(double[] x) {
        int n = x.length;
        double[][] complex = new double[n][2];

        // 실수 신호를 복소수 형태로 변환
        for (int i = 0; i < n; i++) {
            complex[i][0] = x[i];
            complex[i][1] = 0;
        }

        return fft(complex);
    }

    /**
     * 복소수 배열을 실수 배열로 변환 (허수부가 0에 가까운 경우)
     * @param x 복소수 배열
     * @return 실수 배열
     */
    public static double[] toReal(double[][] x) {
        int n = x.length;
        double[] result = new double[n];

        for (int i = 0; i < n; i++) {
            result[i] = x[i][0];
        }

        return result;
    }

    /**
     * FFT 사용 예시
     */
    public static void main(String[] args) {
        // 예제 신호: 사인파 + 코사인파
        int n = 64; // 2의 거듭제곱이어야 함
        double[] signal = new double[n];

        for (int i = 0; i < n; i++) {
            double t = i / (double) n;
            // 10Hz 사인파 + 20Hz 코사인파
            signal[i] = Math.sin(2 * Math.PI * 10 * t) + 0.5 * Math.cos(2 * Math.PI * 20 * t);
        }

        // FFT 수행
        double[][] spectrum = fft(signal);

        // 주파수 스펙트럼의 크기 계산 및 출력
        System.out.println("주파수 스펙트럼 (크기):");
        for (int i = 0; i < n/2; i++) { // 나이퀴스트 주파수까지만 출력
            double magnitude = Math.sqrt(spectrum[i][0] * spectrum[i][0] + spectrum[i][1] * spectrum[i][1]);
            System.out.printf("주파수 %d Hz: %.4f%n", i, magnitude);
        }

        // 역변환 수행
        double[][] complexReconstructed = ifft(spectrum);
        double[] reconstructed = toReal(complexReconstructed);

        // 원본 신호와 역변환 결과 비교
        System.out.println("\n원본 신호와 역변환 결과 비교 (처음 10개 샘플):");
        for (int i = 0; i < 10; i++) {
            System.out.printf("샘플 %d: 원본=%.4f, 복원=%.4f%n", i, signal[i], reconstructed[i]);
        }

        // DFT와 FFT 성능 비교
        long startTime, endTime;

        // DFT 성능 측정
        startTime = System.nanoTime();
        DiscreteFourierTransform.dft(signal);
        endTime = System.nanoTime();
        System.out.printf("\nDFT 실행 시간: %.3f 밀리초%n", (endTime - startTime) / 1_000_000.0);

        // FFT 성능 측정
        startTime = System.nanoTime();
        fft(signal);
        endTime = System.nanoTime();
        System.out.printf("FFT 실행 시간: %.3f 밀리초%n", (endTime - startTime) / 1_000_000.0);
    }
}
```

## 4. 특징

### 4.1 시간 복잡도

- **DFT**: O(N²), 여기서 N은 신호의 샘플 수입니다.
- **FFT**: O(N log N), 이는 DFT보다 훨씬 효율적입니다.

예를 들어, 1024개의 샘플이 있는 신호의 경우:
- DFT: 약 1,048,576번의 연산 필요
- FFT: 약 10,240번의 연산 필요 (약 100배 빠름)

### 4.2 장점

- 신호의 주파수 성분을 분석할 수 있습니다.
- 필터링, 압축, 특징 추출 등 다양한 응용이 가능합니다.
- FFT는 대용량 데이터 처리에 효율적입니다.
- 주파수 도메인에서의 연산이 시간 도메인보다 간단한 경우가 많습니다.

### 4.3 단점

- 구현이 복잡할 수 있습니다.
- FFT는 일반적으로 2의 거듭제곱 길이의 입력을 요구합니다 (일부 변형은 예외).
- 실시간 처리에는 계산 비용이 높을 수 있습니다.

## 5. 응용 분야

푸리에 변환과 FFT는 다양한 분야에서 활용됩니다:

1. **신호 처리**: 오디오 신호 분석, 필터링, 노이즈 제거
2. **이미지 처리**: 이미지 압축, 필터링, 특징 추출
3. **통신 시스템**: 변조, 복조, 스펙트럼 분석
4. **음성 인식**: 음성 신호의 특징 추출
5. **의료 영상**: MRI, CT 스캔 데이터 처리
6. **지진학**: 지진파 분석
7. **금융 분석**: 시계열 데이터 분석
8. **기계 학습**: 특징 추출 및 패턴 인식

## 6. 실제 응용 예시

### 6.1 오디오 스펙트럼 분석기

```java
/**
 * 간단한 오디오 스펙트럼 분석기
 */
public class AudioSpectrumAnalyzer {
    /**
     * 오디오 신호의 스펙트럼을 분석하는 메소드
     * @param audioData 오디오 샘플 데이터
     * @param sampleRate 샘플링 레이트 (Hz)
     */
    public static void analyzeSpectrum(double[] audioData, int sampleRate) {
        // 데이터 길이를 2의 거듭제곱으로 조정
        int n = nearestPowerOfTwo(audioData.length);
        double[] paddedData = new double[n];
        System.arraycopy(audioData, 0, paddedData, 0, Math.min(audioData.length, n));

        // FFT 수행
        double[][] spectrum = FastFourierTransform.fft(paddedData);

        // 주파수 빈(bin) 크기 계산
        double binSize = (double) sampleRate / n;

        // 주요 주파수 성분 찾기
        System.out.println("주요 주파수 성분:");

        // 상위 5개 주파수 성분 찾기
        int[] topIndices = findTopMagnitudes(spectrum, 5, n/2);

        for (int i : topIndices) {
            double frequency = i * binSize;
            double magnitude = Math.sqrt(spectrum[i][0] * spectrum[i][0] + spectrum[i][1] * spectrum[i][1]);
            System.out.printf("주파수: %.2f Hz, 크기: %.4f%n", frequency, magnitude);
        }
    }

    /**
     * 가장 큰 크기를 가진 주파수 인덱스를 찾는 메소드
     * @param spectrum 주파수 스펙트럼
     * @param count 찾을 개수
     * @param maxIndex 검색할 최대 인덱스
     * @return 상위 인덱스 배열
     */
    private static int[] findTopMagnitudes(double[][] spectrum, int count, int maxIndex) {
        int[] indices = new int[count];
        double[] magnitudes = new double[count];

        // 초기화
        for (int i = 0; i < count; i++) {
            indices[i] = -1;
            magnitudes[i] = -1;
        }

        // 각 주파수 빈에 대해
        for (int i = 1; i < maxIndex; i++) { // 0Hz(DC)는 제외
            double magnitude = Math.sqrt(spectrum[i][0] * spectrum[i][0] + spectrum[i][1] * spectrum[i][1]);

            // 현재 상위 목록과 비교
            for (int j = 0; j < count; j++) {
                if (magnitude > magnitudes[j]) {
                    // 나머지 항목 이동
                    for (int k = count - 1; k > j; k--) {
                        indices[k] = indices[k-1];
                        magnitudes[k] = magnitudes[k-1];
                    }

                    // 새 항목 삽입
                    indices[j] = i;
                    magnitudes[j] = magnitude;
                    break;
                }
            }
        }

        return indices;
    }

    /**
     * 가장 가까운 2의 거듭제곱 찾기
     * @param n 원본 숫자
     * @return 가장 가까운 2의 거듭제곱
     */
    private static int nearestPowerOfTwo(int n) {
        int power = 1;
        while (power < n) {
            power *= 2;
        }
        return power;
    }

    /**
     * 사용 예시
     */
    public static void main(String[] args) {
        // 예제 오디오 데이터 생성 (440Hz A 음과 880Hz 하모닉)
        int sampleRate = 44100; // CD 품질
        double duration = 0.1; // 0.1초
        int numSamples = (int) (sampleRate * duration);

        double[] audioData = new double[numSamples];
        for (int i = 0; i < numSamples; i++) {
            double t = i / (double) sampleRate;
            // 440Hz (A 음) + 880Hz (하모닉) + 약간의 노이즈
            audioData[i] = Math.sin(2 * Math.PI * 440 * t) + 
                          0.5 * Math.sin(2 * Math.PI * 880 * t) + 
                          0.1 * Math.random();
        }

        // 스펙트럼 분석
        analyzeSpectrum(audioData, sampleRate);
    }
}
```

### 6.2 이미지 처리 예시

```java
/**
 * 2D FFT를 사용한 간단한 이미지 처리 예시
 * (실제 구현은 더 복잡하지만, 개념을 보여주기 위한 간소화된 버전)
 */
public class ImageFFTExample {
    /**
     * 2D FFT를 수행하는 메소드
     * @param image 그레이스케일 이미지 데이터 (2D 배열)
     * @return 주파수 도메인의 이미지 (3D 배열: [행][열][실수부/허수부])
     */
    public static double[][][] fft2D(double[][] image) {
        int rows = nearestPowerOfTwo(image.length);
        int cols = nearestPowerOfTwo(image[0].length);

        // 패딩된 이미지 생성
        double[][] paddedImage = new double[rows][cols];
        for (int i = 0; i < Math.min(image.length, rows); i++) {
            System.arraycopy(image[i], 0, paddedImage[i], 0, Math.min(image[i].length, cols));
        }

        // 결과 배열 초기화
        double[][][] result = new double[rows][cols][2];

        // 각 행에 대해 1D FFT 수행
        for (int i = 0; i < rows; i++) {
            double[] row = paddedImage[i];
            double[][] rowSpectrum = FastFourierTransform.fft(row);

            for (int j = 0; j < cols; j++) {
                result[i][j][0] = rowSpectrum[j][0];
                result[i][j][1] = rowSpectrum[j][1];
            }
        }

        // 각 열에 대해 1D FFT 수행
        for (int j = 0; j < cols; j++) {
            double[][] column = new double[rows][2];

            for (int i = 0; i < rows; i++) {
                column[i][0] = result[i][j][0];
                column[i][1] = result[i][j][1];
            }

            double[][] colSpectrum = FastFourierTransform.fft(column);

            for (int i = 0; i < rows; i++) {
                result[i][j][0] = colSpectrum[i][0];
                result[i][j][1] = colSpectrum[i][1];
            }
        }

        return result;
    }

    /**
     * 간단한 저역 통과 필터 적용
     * @param spectrum 주파수 도메인의 이미지
     * @param cutoffPercent 차단 주파수 (전체 주파수 범위의 백분율)
     */
    public static void applyLowPassFilter(double[][][] spectrum, double cutoffPercent) {
        int rows = spectrum.length;
        int cols = spectrum[0].length;

        int centerRow = rows / 2;
        int centerCol = cols / 2;
        int cutoffRadius = (int) (Math.min(rows, cols) * cutoffPercent / 100);

        // 주파수 도메인에서 중앙을 원점으로 이동 (FFT shift)
        double[][][] shifted = new double[rows][cols][2];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                int newI = (i + centerRow) % rows;
                int newJ = (j + centerCol) % cols;
                shifted[newI][newJ][0] = spectrum[i][j][0];
                shifted[newI][newJ][1] = spectrum[i][j][1];
            }
        }

        // 저역 통과 필터 적용
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                double distance = Math.sqrt(Math.pow(i - centerRow, 2) + Math.pow(j - centerCol, 2));

                if (distance > cutoffRadius) {
                    shifted[i][j][0] = 0;
                    shifted[i][j][1] = 0;
                }
            }
        }

        // 다시 원래 위치로 이동
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                int newI = (i + centerRow) % rows;
                int newJ = (j + centerCol) % cols;
                spectrum[i][j][0] = shifted[newI][newJ][0];
                spectrum[i][j][1] = shifted[newI][newJ][1];
            }
        }
    }

    /**
     * 가장 가까운 2의 거듭제곱 찾기
     * @param n 원본 숫자
     * @return 가장 가까운 2의 거듭제곱
     */
    private static int nearestPowerOfTwo(int n) {
        int power = 1;
        while (power < n) {
            power *= 2;
        }
        return power;
    }

    /**
     * 사용 예시
     */
    public static void main(String[] args) {
        // 이 예제는 실제 이미지 처리 대신 개념만 설명합니다
        System.out.println("이미지 처리에서의 FFT 응용 예시:");
        System.out.println("1. 이미지의 2D FFT를 계산합니다.");
        System.out.println("2. 주파수 도메인에서 필터링을 수행합니다 (예: 저역 통과 필터).");
        System.out.println("3. 역 FFT를 적용하여 필터링된 이미지를 얻습니다.");
        System.out.println("이를 통해 이미지 노이즈 제거, 엣지 검출, 압축 등이 가능합니다.");
    }
}
```

## 7. 결론

푸리에 변환과 고속 푸리에 변환은 신호 처리와 데이터 분석의 핵심 도구입니다. 이 알고리즘들은 복잡한 신호를 주파수 성분으로 분해하여 분석하고 처리하는 데 필수적입니다.

FFT는 DFT의 계산 효율성을 크게 향상시켜 실시간 신호 처리와 대용량 데이터 분석을 가능하게 했습니다. 이러한 알고리즘의 발전은 디지털 신호 처리, 통신 시스템, 이미지 처리, 음성 인식 등 현대 기술의 많은 부분에 기여하고 있습니다.

Java로 구현된 예제 코드를 통해 푸리에 변환의 기본 원리와 응용 방법을 이해할 수 있습니다. 실제 응용에서는 성능 최적화를 위해 Apache Commons Math, JTransforms 등의 라이브러리를 사용하는 것이 권장됩니다.

푸리에 변환은 수학적으로 복잡할 수 있지만, 그 응용은 우리 일상 생활의 많은 기술에 깊이 관여하고 있습니다. 음악 스트리밍, 이미지 압축, 의료 영상, 날씨 예측 등 다양한 분야에서 푸리에 변환의 원리가 활용되고 있습니다.
