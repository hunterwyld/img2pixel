package com.wanghao;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;

/**
 * @author wanghao
 * @description
 */
public class Main {
    private static final String txt = "$@B%8&WM#*oahkbdpqwmZO0QLCJUYXzcvunxrjft/|()1{}[]?-_+~<>i!lI;:, ^`'. ";

    private static final String usage = "\r\n请依次输入3个参数：原图片路径、像素块大小、输出模式" +
            "\r\n原图片路径：合法的图片路径" +
            "\r\n像素块大小：例如等于10表示以原图片的10x10个像素作为新图片的1个像素" +
            "\r\n输出模式：0代表只输出图片，1代表只输出文字，2代表输出图片和文字";

    public static void main(String[] args) {
        if (args.length != 3) {
            throw new IllegalArgumentException("参数个数非法" + usage);
        }
        // 图片路径
        String inputImagePath = args[0];
        // 像素块大小
        int n = Integer.parseInt(args[1]);
        if (n <= 0) {
            throw new IllegalArgumentException("像素块大小非法" + usage);
        }
        // 0:只输出图片 1:只输出文字 2:图片、文字都输出
        int mode = Integer.parseInt(args[2]);
        if (mode < 0 || mode > 2) {
            throw new IllegalArgumentException("输出模式非法" + usage);
        }

        try {
            BufferedImage oldImage = ImageIO.read(new File(inputImagePath));
            final int width = oldImage.getWidth();
            final int height = oldImage.getHeight();
            final int remainWidth = width - width / n * n;
            final int remainHeight = height - height / n * n;

            BufferedImage newImage = new BufferedImage(oldImage.getWidth(), oldImage.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
            int xStart = oldImage.getMinX();
            int yStart = oldImage.getMinY();
            StringBuilder sb = new StringBuilder();
            while (true) {
                int blockWidth = n;
                int blockHeight = n;
                boolean toResetXStart = false;
                if (xStart + n > width) {
                    blockWidth = remainWidth;
                    toResetXStart = true;
                }
                boolean isLastY = false;
                if (yStart + n > height) {
                    blockHeight = remainHeight;
                    isLastY = true;
                }

                long redSum = 0;
                long greenSum = 0;
                long blueSum = 0;
                // 计算每个n*n块的平均rgb值
                for (int y = yStart; y < yStart + blockHeight; y++) {
                    for (int x = xStart; x < xStart + blockWidth; x++) {
                        int rgb = oldImage.getRGB(x, y);
                        int red = (rgb >> 16) & 0xFF;
                        int green = (rgb >> 8) & 0xFF;
                        int blue = rgb & 0xFF;
                        redSum += red;
                        greenSum += green;
                        blueSum += blue;
                    }
                }
                int block_square = blockWidth * blockHeight;
                if (block_square != 0) {
                    int red = (int) (redSum / block_square);
                    int green = (int) (greenSum / block_square);
                    int blue = (int) (blueSum / block_square);
                    for (int y = yStart; y < yStart + blockHeight; y++) {
                        for (int x = xStart; x < xStart + blockWidth; x++) {
                            int rgb = red*65536 + green*256 + blue;
                            newImage.setRGB(x, y, rgb);
                        }
                    }
                    int gray = (int) (0.2126 * red + 0.7152 * green + 0.0722 * blue);
                    int base = 256 / txt.length() + 1;
                    sb.append(txt.charAt(gray/base)).append(' ');
                }

                // 最后一行，最后一列
                if (toResetXStart && isLastY) {
                    break;
                }

                if (toResetXStart) {
                    sb.append("\r\n");
                    xStart = oldImage.getMinX();
                    yStart += blockHeight;
                } else {
                    xStart += blockWidth;
                }
            }

            if (mode != 1) {
                ImageIO.write(newImage, "jpg", new File("output.jpg"));
                System.out.println("written to output.jpg");
            }
            if (mode != 0) {
                FileWriter fw = new FileWriter(new File("output.txt"));
                fw.write(sb.toString());
                fw.close();
                System.out.println("written to output.txt");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
