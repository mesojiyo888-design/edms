<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

<head>
    <meta http-equiv="Content-Type" content="text/html;charset=UTF-8"/>
    <style>
        .editor-box {
            display: none;
            margin-top: 20px;
        }

        .editor-box.active {
            display: block;
        }

        .btn-area button {
            margin: 4px;
        }
    </style>

    <script>
        function showEditorDiv(divId) {
            var boxes = document.querySelectorAll('.editor-box');

            boxes.forEach(function(box) {
                box.style.display = 'none';
            });

            var target = document.getElementById(divId);
            if (target) {
                target.style.display = 'block';
            }
        }

        document.addEventListener('DOMContentLoaded', function() {
            showEditorDiv('editor');
        });
    </script>

    <script>
        var content = '초기 내용';

        const { Editor } = toastui;

         $(function() {
             const editor = new Editor({
                el: document.querySelector('#editor'),
                previewStyle: 'vertical',
                height: '500px',
                initialValue: content
             });
             const editor2 = new Editor({
                el: document.querySelector('#EditorWithHorizontalPreview'),
                previewStyle: 'tab',
                height: '500px',
                initialValue: content
             });
             const editor3 = new Editor({
                el: document.querySelector('#EditorWithWYSIWYGMode'),
                height: '500px',
                initialValue: content,
                initialEditType: 'wysiwyg'
             });

             const viewer4 = new Editor({
                el: document.querySelector('#Viewer'),
                initialValue: content
             });

             const viewer5 = Editor.factory({
                el: document.querySelector('#ViewerUsingEditorFactory'),
                viewer: true,
                initialValue: content
             });

             const editor6 = new Editor({
                el: document.querySelector('#EditorwithDarkTheme'),
                previewStyle: 'vertical',
                height: '500px',
                initialValue: content,
                theme: 'dark'
             });

             // const chart = Editor.plugin.chart;
             //
             // const chartOptions = {
             //    minWidth: 100,
             //    maxWidth: 600,
             //    minHeight: 100,
             //    maxHeight: 300
             // };
             //
             // const editor7 = new Editor({
             //    el: document.querySelector('#EditorwithChartPlugin'),
             //    previewStyle: 'vertical',
             //    height: '500px',
             //    initialValue: chartContent,
             //    plugins: [[chart, chartOptions]]
             // });
         });


    </script>


</head>
<body style="text-align:center; margin:0 auto; display:inline; padding-top:100px;">
<div class="btn-area">
    <button type="button" onclick="showEditorDiv('editor')">기본 Editor</button>
    <button type="button" onclick="showEditorDiv('EditorWithHorizontalPreview')">Horizontal Preview</button>
    <button type="button" onclick="showEditorDiv('EditorWithWYSIWYGMode')">WYSIWYG Mode</button>
    <button type="button" onclick="showEditorDiv('Viewer')">Viewer수정 중</button>
    <button type="button" onclick="showEditorDiv('ViewerUsingEditorFactory')">Viewer Factory</button>
    <button type="button" onclick="showEditorDiv('EditorwithDarkTheme')">Dark Theme</button>
    <button type="button" onclick="showEditorDiv('EditorwithChartPlugin')">Chart Plugin</button>
    <button type="button" onclick="showEditorDiv('EditorwithCodeSyntaxHighlightPlugin')">Code Syntax Highlight</button>
</div>

<div id="editor" class="editor-box"></div>
<div id="EditorWithHorizontalPreview" class="editor-box"></div>
<div id="EditorWithWYSIWYGMode" class="editor-box"></div>
<div id="Viewer" class="editor-box"></div>
<div id="ViewerUsingEditorFactory" class="editor-box"></div>
<div id="EditorwithDarkTheme" class="editor-box"></div>
<div id="EditorwithChartPlugin" class="editor-box"></div>
<div id="EditorwithCodeSyntaxHighlightPlugin" class="editor-box"></div>
</body>
</html>
