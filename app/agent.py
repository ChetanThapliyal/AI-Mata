"""
AI Mata (Maya) — Vision Agent Backend

A real-time vision agent powered by the GetStream Vision Agents SDK.
Maya watches through the camera, listens to you, and responds with
witty, sarcastic, overprotective Indian-mother energy.

Stack:
  - gemini.VLM       → Analyzes live video frames (food, room, etc.)
  - deepgram.STT      → Transcribes user speech
  - elevenlabs.TTS    → Maya speaks back with a real voice
  - getstream.Edge    → Real-time video/audio transport (WebRTC)
  - JWT middleware    → Protects /session/start from unauthenticated access
"""

import logging
import os

import jwt
from dotenv import load_dotenv
from fastapi import Request, status
from fastapi.responses import JSONResponse
from starlette.middleware.base import BaseHTTPMiddleware
from vision_agents.core import Agent, AgentLauncher, Runner, User
from vision_agents.plugins import deepgram, elevenlabs, gemini, getstream

from .prompts import MOTHER_SYSTEM_PROMPT

logger = logging.getLogger(__name__)

load_dotenv()

# Secret used to verify JWT tokens from the Android app.
# We reuse the STREAM_API_SECRET so the Android app can generate
# tokens without needing a separate secret.
API_SECRET = os.environ.get("STREAM_API_SECRET", "")


class JWTAuthMiddleware(BaseHTTPMiddleware):
    """Protects /session/start with JWT authentication."""

    async def dispatch(self, request: Request, call_next):
        # Only protect the /sessions endpoint (the Vision Agents SDK route for joining calls)
        if request.url.path == "/sessions" and request.method == "POST":
            auth_header = request.headers.get("Authorization")
            if not auth_header or not auth_header.startswith("Bearer "):
                return JSONResponse(
                    status_code=status.HTTP_401_UNAUTHORIZED,
                    content={"detail": "Missing or invalid Authorization header"},
                )

            token = auth_header.split(" ", 1)[1]
            try:
                jwt.decode(token, API_SECRET, algorithms=["HS256"])
            except jwt.ExpiredSignatureError:
                return JSONResponse(
                    status_code=status.HTTP_401_UNAUTHORIZED,
                    content={"detail": "Token has expired"},
                )
            except jwt.InvalidTokenError:
                return JSONResponse(
                    status_code=status.HTTP_401_UNAUTHORIZED,
                    content={"detail": "Invalid token"},
                )

        return await call_next(request)


async def create_agent(**kwargs) -> Agent:
    """
    Factory function called by the Runner/AgentLauncher to create a new Maya agent
    for each incoming session/call.
    """

    # Vision Language Model — watches video frames and understands visual context
    vlm = gemini.VLM(
        model="gemini-2.0-flash",
        fps=1,
        frame_buffer_seconds=5,
    )

    agent = Agent(
        edge=getstream.Edge(),
        agent_user=User(name="Maya", id="maya-ai-mata"),
        instructions=MOTHER_SYSTEM_PROMPT,
        processors=[],
        llm=vlm,
        tts=elevenlabs.TTS(),
        stt=deepgram.STT(eager_turn_detection=True),
    )

    return agent


async def join_call(agent: Agent, call_type: str, call_id: str, **kwargs) -> None:
    """
    Called when a session starts. The agent joins the GetStream call
    and sends an initial greeting as Maya.
    """
    call = await agent.create_call(call_type, call_id)

    async with agent.join(call):
        # Maya's opening line when she first connects
        await agent.simple_response(
            "You just connected to the camera. Look at what's in front of you "
            "and give your first reaction as Maya — comment on the room, "
            "any food you see, or whatever catches your eye first."
        )
        await agent.finish()


def build_runner() -> Runner:
    runner = Runner(
        AgentLauncher(
            create_agent=create_agent,
            join_call=join_call,
        )
    )
    # Attach JWT middleware to the live FastAPI app the Runner serves
    runner.fast_api.add_middleware(JWTAuthMiddleware)
    return runner


# Entry point: `python -m app.agent serve/run` via Runner.cli()
if __name__ == "__main__":
    runner = build_runner()
    runner.cli()
