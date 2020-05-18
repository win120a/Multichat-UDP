/*
    Copyright (C) 2011-2020 Andy Cheung

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/

package ac.adproj.mchat.ui;

import java.util.function.Predicate;

import javax.swing.JOptionPane;

/**
 * 对 JOptionPane 的对话框显示方法进行封装的类。
 * 
 * @author Andy Cheung
 */
public final class CommonDialogs {

    /**
     * 显示输入框。
     * 
     * @param askMessage   询问时的信息。
     * @param errorMessage 错误时向用户提示的信息。
     * @return 用户输入
     */
    public static String inputDialog(String askMessage, String errorMessage) {
        return inputDialog(null, askMessage, errorMessage);
    }

    /**
     * 显示带默认输入的输入框。
     * 
     * @param defaultString 输入框默认值。
     * @param askMessage    询问时的信息。
     * @param errorMessage  错误时向用户提示的信息。
     * @return 用户输入
     */
    public static String inputDialog(String defaultString, String askMessage, String errorMessage) {
        return inputDialog(defaultString, askMessage, errorMessage, (a) -> true);
    }

    /**
     * 显示带默认输入的输入框，并对用户输入进行检查。
     * 
     * @param defaultString 输入框默认值。
     * @param askMessage    询问时的信息。
     * @param errorMessage  错误时向用户提示的信息。
     * @param filter        用户输入的过滤器，用于检查是否输入正确。
     * @return 用户输入
     */
    public static String inputDialog(String defaultString, String askMessage, String errorMessage,
            Predicate<String> filter) {
        String response;
        int counter = 0;

        do {
            response = (String) JOptionPane.showInputDialog(null, askMessage, "Input", JOptionPane.QUESTION_MESSAGE,
                    null, null, defaultString);
            counter++;
        } while ((response == null || response.trim().isEmpty() || !filter.test(response)) && counter <= 5);   // 反复询问

        if (counter > 5) {
            errorDialog(errorMessage);
            System.exit(-1);
        }

        return response;
    }

    /**
     * 显示出错对话框。
     * 
     * @param message 显示的消息
     */
    public static void errorDialog(String message) {
        JOptionPane.showMessageDialog(null, message, "ERROR", JOptionPane.ERROR_MESSAGE);
    }
}
