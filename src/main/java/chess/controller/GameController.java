package chess.controller;

import chess.domain.Command;
import chess.domain.piece.Team;
import chess.service.ChessService;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class GameController {

    private final ChessService chessService;

    public GameController(ChessService chessService) {
        this.chessService = chessService;
    }

    @GetMapping("/game/{id}")
    public String game(@PathVariable Long id,
                       @RequestParam(value = "error", required = false) String error,
                       Model model) {
        List<String> chessBoard = chessService.findChessBoardById(id);

        model.addAttribute("chessboard", chessBoard);
        model.addAttribute("id", id);
        model.addAttribute("error", error);

        return "chess";
    }

    @PostMapping("/game/{id}/move")
    public String move(@PathVariable Long id,
                       @RequestParam("from") String from, @RequestParam("to") String to,
                       Model model) {
        model.addAttribute("id", id);

        String command = makeCommand(from, to);
        chessService.move(id, command);

        if (chessService.isEnd(id)) {
            return "redirect:/game/" + id + "/end";
        }
        return "redirect:/game/" + id;
    }

    @GetMapping("/game/{id}/end")
    public String end(@PathVariable Long id, Model model) {
        String winTeamName = chessService.finish(id, Command.from("end"));
        List<String> chessBoard = chessService.getCurrentChessBoard(id);

        model.addAttribute("winTeam", winTeamName);
        model.addAttribute("chessboard", chessBoard);
        model.addAttribute("id", id);

        return "chess";
    }

    @GetMapping("/game/{id}/status")
    public String status(@PathVariable Long id, Model model) {
        Map<Team, Double> score = chessService.getScore(id);
        List<String> chessBoard = chessService.getCurrentChessBoard(id);

        model.addAttribute("blackScore", score.get(Team.BLACK));
        model.addAttribute("whiteScore", score.get(Team.WHITE));
        model.addAttribute("chessboard", chessBoard);
        model.addAttribute("id", id);

        return "chess";
    }

    private String makeCommand(String from, String to) {
        return "move " + from + " " + to;
    }
}
