package chess.service;

import chess.dao.ChessGameDao;
import chess.dao.PieceDao;
import chess.domain.ChessGame;
import chess.domain.Command;
import chess.domain.piece.Team;
import chess.domain.state.State;
import chess.dto.ChessGameDto;
import chess.exception.IllegalGameProgressException;
import java.util.List;
import java.util.Map;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

@Service
public class ChessService {

    private final ChessGameDao chessGameDao;
    private final PieceDao pieceDao;

    public ChessService(ChessGameDao chessGameDao, PieceDao pieceDao) {
        this.chessGameDao = chessGameDao;
        this.pieceDao = pieceDao;
    }

    public List<String> getCurrentChessBoard(Long id) {
        ChessGame chessGame = chessGameDao.findById(id);
        return chessGame.getChessBoardSymbol();
    }

    public Long save(String gameName, String password) {
        validateDuplicateGameName(gameName);

        ChessGame chessGame = new ChessGame(gameName);
        chessGame.progress(Command.from("start"));
        ChessGameDto chessGameDto = ChessGameDto.from(chessGame);

        Long id = chessGameDao.save(chessGameDto, password);
        pieceDao.save(id, chessGameDto);

        return id;
    }

    private void validateDuplicateGameName(String gameName) {
        try {
            chessGameDao.findOneByGameName(gameName);
            throw new IllegalGameProgressException("동일한 이름의 게임이 이미 존재합니다");
        } catch (EmptyResultDataAccessException ignored) {
        }
    }

    public List<String> move(Long id, String moveCommand) {
        ChessGame chessGame = chessGameDao.findById(id);
        Command command = Command.from(moveCommand);

        chessGame.progress(command);
        updateChessGame(id, ChessGameDto.from(chessGame));

        return chessGame.getChessBoardSymbol();
    }

    private void updateChessGame(Long id, ChessGameDto chessGameDto) throws IllegalStateException {
        chessGameDao.update(id, chessGameDto);
        pieceDao.update(id, chessGameDto);
    }

    public Map<Team, Double> getScore(Long id) {
        ChessGame chessGame = chessGameDao.findById(id);
        return chessGame.calculateResult();
    }

    public String finish(Long id, Command command) {
        ChessGame chessGame = chessGameDao.findById(id);

        chessGame.progress(command);
        updateChessGame(id, ChessGameDto.from(chessGame));

        return chessGame.getWinTeamName();
    }

    public List<String> findChessBoardById(Long id) throws IllegalStateException {
        ChessGame chessGame = chessGameDao.findById(id);

        return chessGame.getChessBoardSymbol();
    }

    public boolean isEnd(Long id) {
        ChessGame chessGame = chessGameDao.findById(id);
        return chessGame.isEnd();
    }

    public List<String> findAllGameName() {
        return chessGameDao.findAllGameName();
    }

    public Long findIdByGameName(String gameName) {
        return chessGameDao.findIdByGameName(gameName);
    }

    public void deleteBy(String gameName, String password) {
        State state = findStateBy(gameName, password);
        deleteEndGame(state, gameName, password);
    }

    private State findStateBy(String gameName, String password) {
        try {
            return chessGameDao.findStateByGameNameAndPassword(gameName, password);
        } catch(EmptyResultDataAccessException e) {
            throw new IllegalGameProgressException("비밀번호가 틀렸습니다.");
        }
    }

    private int deleteEndGame(State state, String gameName, String password) {
        if (!state.isEnd()) {
            throw new IllegalGameProgressException("게임이 종료되어야 삭제가 가능합니다.");
        }
        return chessGameDao.deleteByGameNameAndPassword(gameName, password);
    }
}
