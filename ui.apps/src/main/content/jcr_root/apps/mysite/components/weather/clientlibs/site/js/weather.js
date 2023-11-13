const temperatureElement = document.getElementById('temperature');
let dataSet
let slideIndex = 1;

$(document).ready(function () {
    setData()
    function setData() {
        setTimeout(() => {
            showSlides(slideIndex);
        }, 500);
    }
});

function changeToCelcius() {
    document.getElementById("tempratureDiv").style.display="block";
    document.getElementById("tempratureDivF").style.display="none";
}

function changeToFarenheit() {
    document.getElementById("tempratureDiv").style.display="none";
    document.getElementById("tempratureDivF").style.display="block";
}

function plusSlides(n) {
    showSlides(slideIndex += n);
}

function currentSlide(n) {
    showSlides(slideIndex = n);
}

function showSlides(n) {
    let i;
    let slides = document.getElementsByClassName("mySlides");
    let dots = document.getElementsByClassName("dot");
    if (n > slides.length) { slideIndex = 1 }
    if (n < 1) { slideIndex = slides.length }
    for (i = 0; i < slides.length; i++) {
        slides[i].style.display = "none";
    }

    slides[slideIndex - 1].style.display = "block";
}
