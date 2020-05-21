package pl.starchasers.mdpages.index

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@Controller
class IndexController(){

    @GetMapping("/")
    fun index(): String = "index.html"

}