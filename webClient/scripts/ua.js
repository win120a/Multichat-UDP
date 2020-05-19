/*  AC UA Library  */

/*
   Copyright (C) 2011-2020 Andy Cheung

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

const BROWSER_CHROME = 1;
const BROWSER_EDGE = 2;
const BROWSER_IE = 3;
const BROWSER_FIREFOX = 4;
const BROWSER_SAFARI = 5;
const BROWSER_UNKNOWN = -1;

const TERMINAL_WINDOWS = 1;
const TERMINAL_ANDROID = 2;
const TERMINAL_IOS = 3;
const TERMINAL_WP = 4;
const TERMINAL_LINUX = 5;
const TERMINAL_UNKNOWN = -1;

function getBrowserType() {
    let ua = navigator.userAgent;

    if (ua.match(/Edge\/(\d.)+/) != null) {
        return BROWSER_EDGE;
    } else if (ua.match(/Chrome\/(\d.)+/) != null) {   // 在JS中，/.../ 代表正则表达式
        return BROWSER_CHROME;
    } else if (ua.match(/Trident\/(\d.)+/) != null) {
        return BROWSER_IE;
    } else if (ua.match(/Firefox\/(\d.)+/) != null) {
        return BROWSER_FIREFOX;
    } else if (ua.match(/Safari\/(\d.)+/) != null){
        return BROWSER_SAFARI;
    } else {
        return BROWSER_UNKNOWN;
    }
}

function getTerminalType() {
    let ua = navigator.userAgent;

    if (ua.match(/iPad;/) != null || ua.match(/iPhone;/) != null) {
        return TERMINAL_IOS;
    } else if (ua.match(/Android (\d.)+/) != null) {
        return TERMINAL_ANDROID;
    } else if (ua.match(/Windows/) != null) {
        return TERMINAL_WINDOWS;
    } else if (ua.match(/Linux/) != null){
        return TERMINAL_LINUX;
    } else if (ua.match(/Windows Phone (\d.)+/) != null) {
        return TERMINAL_WP;
    } else {
        return TERMINAL_UNKNOWN;
    }
}

function isMobileDevice()
{
    const type = getTerminalType();
    return type == TERMINAL_ANDROID || type == TERMINAL_WP || type == TERMINAL_IOS;
}