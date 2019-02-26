$(function() {

  function show(id) {
    $('#new').hide();
    $('#upload').hide();
    $('#view').hide();
    $('#' + id).show();
  }
  function updateImage(image) {
    document.title = 'Imagery | #' + image.id;
    $('#upload form').attr("action", "/image/" + image.id + "/upload");
    $('#upload blockquote').html("state " + image.state);
    $('#view img').attr("src", image.processedImage);
    $('#view blockquote').html("state " + image.state);
  }
  
  var hash = window.location.hash.substr(1).split("=");
  if (window.location.hash.length > 0) {
    if (hash[0] === 'upload' && hash.length === 2) {
      $.get('/image/' + hash[1], function(data) {
        updateImage(data);
        show('upload');
      })
      .fail(function() {
        alert('error');
      });
    } else if (hash[0] === 'view' && hash.length === 2) {
      $.get('/image/' + hash[1], function(data) {
        updateImage(data);
        show('view');
      })
      .fail(function() {
        alert('error');
      });
    } else {
      show('new');
    }
  } else {
    show('new');
  }

  $('#new a').click(function() {
    $.post('/image', function(data) {
        updateImage(data);
        show('upload');
        window.location.hash = '#upload='+ data.id;
      })
      .fail(function() {
        alert('error');
      });
    return false;
  });
  $('#view a.refresh').click(function() {
    $.get('/image/' + hash[1], function(data) {
        updateImage(data);
      })
      .fail(function() {
        alert('error');
      });
    return false;
  });
});
