var ToastEditorManager = (function() {
    var instances = {};

    return {
        init: function(editorSelector, formSelector, options) {
            var el = document.querySelector(editorSelector);

            if (!el) {
                console.error('Toast Editor 영역을 찾을 수 없습니다. selector:', editorSelector);
                return;
            }

            options = options || {};

            var defaultOptions = {
                el: el,
                height: '500px',
                previewStyle: 'vertical',
                initialEditType: 'markdown',
                initialValue: '',
                usageStatistics: false
            };

            var finalOptions = this.mergeOptions(defaultOptions, options);

            finalOptions.hooks = finalOptions.hooks || {};

            /*
             * 이미지 업로드/붙여넣기 시 실행
             * - blob을 JS 메모리 tempImages 배열에 임시 저장
             * - Editor 화면에는 blob URL로 미리보기 표시
             * - 실제 저장 버튼 클릭 시 saveWithImages()에서 MultipartFile로 Java에 전송
             */
            finalOptions.hooks.addImageBlobHook = function(blob, callback) {
                ToastEditorManager.addTempImage(editorSelector, blob, callback);
                return false;
            };

            var editor = new toastui.Editor(finalOptions);

            instances[editorSelector] = {
                editor: editor,
                formSelector: formSelector,
                options: finalOptions,
                tempImages: []
            };

            return editor;
        },

        addTempImage: function(editorSelector, blob, callback) {
            var item = instances[editorSelector];

            if (!item) {
                alert('Editor 인스턴스를 찾을 수 없습니다.');
                return;
            }

            var tempImageId = 'temp_' + new Date().getTime() + '_' + item.tempImages.length;
            var fileName = blob.name || 'editor-image.png';
            var contentType = blob.type || 'application/octet-stream';
            var blobUrl = URL.createObjectURL(blob);

            /*
             * JS단 임시 Blob 저장
             */
            item.tempImages.push({
                tempImageId: tempImageId,
                fileName: fileName,
                contentType: contentType,
                fileSize: blob.size,
                blob: blob,
                blobUrl: blobUrl
            });

            /*
             * Editor 화면에는 임시 blob URL로 미리보기 표시
             * Editor Markdown에는 아래와 같은 값이 들어감
             * ![editor-image.png](blob:http://localhost:8080/....)
             */
            callback(blobUrl, fileName);
        },

        saveWithImages: function(editorSelector, hiddenSelector, saveUrl) {
            var item = instances[editorSelector];

            if (!item) {
                alert('Editor 인스턴스를 찾을 수 없습니다.');
                return false;
            }

            var hiddenEl = document.querySelector(hiddenSelector);

            if (!hiddenEl) {
                alert('hidden input을 찾을 수 없습니다.');
                return false;
            }

            var formEl = document.querySelector(item.formSelector);

            if (!formEl) {
                alert('form을 찾을 수 없습니다.');
                return false;
            }

            var markdown = item.editor.getMarkdown();

            if (!markdown || !markdown.trim()) {
                alert('내용을 입력하세요.');
                return false;
            }

            hiddenEl.value = markdown;

            /*
             * form 내부의 일반 input까지 같이 전송
             */
            var formData = new FormData(formEl);

            /*
             * hidden name이 content가 아닐 수도 있으므로 안전하게 한 번 더 추가
             */
            formData.set(hiddenEl.name || 'content', markdown);

            /*
             * JS단에 임시 저장된 Blob들을 Java MultipartFile로 전송
             */
            for (var i = 0; i < item.tempImages.length; i++) {
                formData.append('images', item.tempImages[i].blob, item.tempImages[i].fileName);
                formData.append('tempImageIds', item.tempImages[i].tempImageId);
                formData.append('blobUrls', item.tempImages[i].blobUrl);
            }

            $.ajax({
                url: saveUrl,
                type: 'POST',
                data: formData,
                processData: false,
                contentType: false,
                success: function(res) {
                    console.log('저장 성공:', res);
                    alert('저장되었습니다.');
                },
                error: function(xhr) {
                    console.error('status:', xhr.status);
                    console.error('url:', saveUrl);
                    console.error('response:', xhr.responseText);
                    alert('저장 중 오류가 발생했습니다.');
                }
            });

            return true;
        },

        getEditor: function(editorSelector) {
            var item = instances[editorSelector];

            if (!item) {
                return null;
            }

            return item.editor;
        },

        changeMode: function(editorSelector, mode) {
            var item = instances[editorSelector];

            if (!item) {
                return;
            }

            item.editor.changeMode(mode);
        },

        destroy: function(editorSelector) {
            var item = instances[editorSelector];

            if (item) {
                if (item.editor) {
                    item.editor.destroy();
                }

                if (item.tempImages) {
                    for (var i = 0; i < item.tempImages.length; i++) {
                        if (item.tempImages[i].blobUrl) {
                            URL.revokeObjectURL(item.tempImages[i].blobUrl);
                        }
                    }
                }

                delete instances[editorSelector];
            }
        },

        mergeOptions: function(defaultOptions, userOptions) {
            var result = {};
            var key;

            for (key in defaultOptions) {
                if (defaultOptions.hasOwnProperty(key)) {
                    result[key] = defaultOptions[key];
                }
            }

            for (key in userOptions) {
                if (userOptions.hasOwnProperty(key)) {
                    result[key] = userOptions[key];
                }
            }

            return result;
        }
    };
})();